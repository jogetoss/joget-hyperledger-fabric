package org.joget.hyperledger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class to integrate with Hyperledger Fabric using its Java SDK
 */
public class FabricUtil {

    /**
     * Get new fabric-ca client
     *
     * @param caUrl The fabric-ca-server endpoint url
     * @param caCert The fabri-ca certificate
     * @return new client instance. never null.
     * @throws Exception
     */
    public static HFCAClient getHfCaClient(String caUrl, String caCert) throws Exception {
        Properties caClientProperties = new Properties();
        if (caCert != null) {
            caClientProperties.put("pemBytes", caCert.getBytes());
        }
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFCAClient caClient = HFCAClient.createNewInstance(caUrl, caClientProperties);
        caClient.setCryptoSuite(cryptoSuite);
        return caClient;
    }

    /**
     * Create new HLF client
     *
     * @return new HLF client instance. Never null.
     * @throws CryptoException
     * @throws InvalidArgumentException
     */
    public static HFClient getHfClient() throws Exception {
        // initialize default cryptosuite
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // setup the client
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(cryptoSuite);
        return client;
    }

    /**
     * Register and enroll user with userId. If FabricUser object with the name
     * already exists in the filesystem it will be loaded and registration and enrollment
     * will be skipped.
     *
     * @param caClient The fabric-ca client.
     * @param registrar The registrar to be used.
     * @param userId The user id.
     * @param affiliation
     * @param mspId
     * @return FabricUser instance with userId, affiliation,mspId and enrollment
     * set.
     * @throws Exception
     */
    public static FabricUser getUser(HFCAClient caClient, FabricUser registrar, String userId, String affiliation, String mspId) throws Exception {
        FabricUser appUser = deserialize(userId);
        if (appUser == null) {
            RegistrationRequest rr = new RegistrationRequest(userId, affiliation);
            String enrollmentSecret = caClient.register(rr, registrar);
            Enrollment enrollment = caClient.enroll(userId, enrollmentSecret);
            appUser = new FabricUser(userId, affiliation, mspId, enrollment);
            serialize(appUser);
        }
        return appUser;
    }
    
    /**
     * Return an FabricUser object with the provided certificate and private key.
     * @param userId
     * @param affiliation
     * @param mspId
     * @param cert
     * @param pemPrivateKey
     * @return
     * @throws Exception 
     */
    public static FabricUser getUser(String userId, String affiliation, String mspId, final String cert, final String pemPrivateKey) throws Exception {
        FabricUser appUser = new FabricUser(userId, affiliation, mspId, null);    
        final PrivateKey privateKey = loadPrivateKey(pemPrivateKey);
        appUser.setEnrollment(new Enrollment() {
            @Override
            public PrivateKey getKey() {
                return privateKey;
            }

            @Override
            public String getCert() {
                return cert;
            }
        });
        return appUser;
    }
    
    /**
     * Enroll admin into fabric-ca using {@code userId/secret} credentials. If
     * FabricUser object already exists in the filesystem it will be loaded and new
     * enrollment will not be executed.
     *
     * @param caClient The fabric-ca client
     * @param userId
     * @param secret
     * @param affiliation
     * @param mspId
     * @return FabricUser instance with userid, affiliation, mspId and enrollment
     * set
     * @throws Exception
     */
    public static FabricUser getAdmin(HFCAClient caClient, String userId, String secret, String affiliation, String mspId) throws Exception {
        FabricUser admin = deserialize(userId);
        if (admin == null) {
            Enrollment adminEnrollment = caClient.enroll(userId, secret);
            admin = new FabricUser(userId, affiliation, mspId, adminEnrollment);
            serialize(admin);
        }
        return admin;
    }

    /**
     * Save FabricUser object as JSON to file
     *
     * @param appUser The object to be serialized
     * @throws IOException
     */
    public static void serialize(FabricUser appUser) throws IOException {
        String json = new Gson().toJson(appUser);
        String fileName = "hf_" + appUser.getName() + ".json";
        String dirPath = getBaseDirectory() + "/hf_users";
        FileUtils.writeStringToFile(new File(dirPath, fileName), json, "UTF-8");
        String pemPrivateKey = getPemFromPrivateKey(appUser.getEnrollment().getKey());
        String privateKeyFileName = "hf_" + appUser.getName() + ".pem";
        FileUtils.writeStringToFile(new File(dirPath, privateKeyFileName), pemPrivateKey, "UTF-8");
    }

    /**
     * Read FabricUser object from JSON file
     * @param name
     * @return
     * @throws Exception 
     */
    public static FabricUser deserialize(String name) throws Exception {
        String fileName = "hf_" + name + ".json";
        String dirPath = getBaseDirectory() + "/hf_users";
        File jsonFile = new File(dirPath, fileName);
        if (!jsonFile.exists()) {
            return null;
        }
        String json = FileUtils.readFileToString(jsonFile, "UTF-8");

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Enrollment.class, new InstanceCreator<Enrollment>() {
            @Override
            public Enrollment createInstance(Type type) {
                return new Enrollment() {
                    String cert;

                    @Override
                    public PrivateKey getKey() {
                        return null;
                    }

                    public void setKey(PrivateKey key) {
                    }

                    @Override
                    public String getCert() {
                        return this.cert;
                    }

                    public void setCert(String cert) {
                        this.cert = cert;
                    }
                };
            }
        });

        FabricUser appUser = builder.create().fromJson(json, FabricUser.class);
        String privateKeyFileName = "hf_" + appUser.getName() + ".pem";
        String pemPrivateKey = FileUtils.readFileToString(new File(dirPath, privateKeyFileName), "UTF-8");
        final PrivateKey privateKey = loadPrivateKey(pemPrivateKey);
        final String cert = appUser.getEnrollment().getCert();
        appUser.setEnrollment(new Enrollment() {
            @Override
            public PrivateKey getKey() {
                return privateKey;
            }

            @Override
            public String getCert() {
                return cert;
            }
        });
        return appUser;
    }

    public static String getBaseDirectory() {
        return SetupManager.getBaseDirectory();
//        return System.getProperty(SYSTEM_PROPERTY_WFLOW_HOME, System.getProperty("user.home") + File.separator + "wflow" + File.separator);
    }

    public static void logInfo(String message) {
        LogUtil.info(FabricUtil.class.getName(), message);
//        System.out.println("INFO: " + message);
    }

    public static void logDebug(String message) {
        LogUtil.debug(FabricUtil.class.getName(), message);
//        System.out.println("DEBUG: " + message);
    }

    public static void logError(Exception e, String message) {
        LogUtil.error(FabricUtil.class.getName(), e, message);
//        System.err.println("ERROR: " + message);
//        e.printStackTrace(System.err);
    }
    
    /**
     * Initialize and get HF channel
     *
     * @param client The HFC client
     * @param peerName
     * @param peerUrl
     * @param peerCert
     * @param ordererName
     * @param ordererUrl
     * @param ordererCert
     * @param channelName
     * @return Initialized channel
     * @throws InvalidArgumentException
     * @throws TransactionException
     */
    public static Channel getChannel(HFClient client, String peerName, String peerUrl, String peerCert, String ordererName, String ordererUrl, String ordererCert, String channelName) throws InvalidArgumentException, TransactionException {
        // initialize channel
        Properties peerProps = new Properties();
        if (peerCert != null) {
            peerProps.put("pemBytes", peerCert.getBytes());
            peerProps.put("hostnameOverride", peerName);
        }
        // peer name and endpoint in fabcar network
        Peer peer = client.newPeer(peerName, peerUrl, peerProps);
        Channel channel = client.newChannel(channelName);
        Channel.PeerOptions peerOptions = Channel.PeerOptions.createPeerOptions().createPeerOptions()
                                .setPeerRoles(EnumSet.allOf(Peer.PeerRole.class));
        channel.addPeer(peer, peerOptions);
        
        if (ordererName != null && ordererUrl != null) {
            // orderer name and endpoint in fabcar network
            Properties ordererProps = new Properties();
            if (ordererCert != null) {
                ordererProps.put("pemBytes", ordererCert.getBytes());        
                ordererProps.put("hostnameOverride", ordererName);
            }
            Orderer orderer = client.newOrderer(ordererName, ordererUrl, ordererProps);
            channel.addOrderer(orderer);
        }
        channel.initialize();
        return channel;
    }

    /**
     * Invoke blockchain query
     *
     * @param client The HF Client
     * @param channelName
     * @param chainCodeId
     * @param query
     * @param args
     * @return 
     * @throws ProposalException
     * @throws InvalidArgumentException
     */
    public static String queryBlockChain(HFClient client, String channelName, String chainCodeId, String query, String[] args) throws ProposalException, InvalidArgumentException {
        // get channel instance from client
        Channel channel = client.getChannel(channelName);
        // create chaincode request
        QueryByChaincodeRequest qpr = client.newQueryProposalRequest();
        // build cc id providing the chaincode name. Version is omitted here.
        qpr.setChaincodeName(chainCodeId);
        // CC function to be called
        qpr.setFcn(query);
        if (args != null) {
            qpr.setArgs(args);
        }
        Collection<ProposalResponse> res = channel.queryByChaincode(qpr);
        // return response
        String response = "";
        for (ProposalResponse pres : res) {
            String stringResponse = new String(pres.getChaincodeActionResponsePayload());
            response += stringResponse;
        }
        return response;
    }

    /**
     * Invoke blockchain update
     *
     * @param client
     * @param channel
     * @param chaincodeId
     * @param function
     * @param args
     * @throws ProposalException
     * @throws InvalidArgumentException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static void updateBlockChain(HFClient client, Channel channel, String chaincodeId, String function, String[] args)
            throws ProposalException, InvalidArgumentException, InterruptedException, ExecutionException, TimeoutException {
        BlockEvent.TransactionEvent event = sendTransaction(client, channel, chaincodeId, function, args).get(60, TimeUnit.SECONDS);
        if (event.isValid()) {
            logInfo("Transaction tx: " + event.getTransactionID() + " is completed.");
        } else {
            logInfo("Transaction tx: " + event.getTransactionID() + " is invalid.");
        }
    }

    /**
     * Send transaction proposal to HF peer
     * @param client
     * @param channel
     * @param chaincodeId
     * @param function
     * @param args
     * @return
     * @throws InvalidArgumentException
     * @throws ProposalException 
     */
    public static CompletableFuture<BlockEvent.TransactionEvent> sendTransaction(HFClient client, Channel channel, String chaincodeId, String function, String[] args)
            throws InvalidArgumentException, ProposalException {
        TransactionProposalRequest tpr = client.newTransactionProposalRequest();
        tpr.setChaincodeName(chaincodeId);
        tpr.setFcn(function);
        tpr.setArgs(args);
        tpr.setProposalWaitTime(1000);
        Collection<ProposalResponse> responses = channel.sendTransactionProposal(tpr);
        List<ProposalResponse> invalid = new ArrayList<>();
        for (ProposalResponse pr : responses) {
            if (pr.isInvalid()) {
                logInfo(pr.getMessage());
                invalid.add(pr);
            }
        }
        if (!invalid.isEmpty()) {
            throw new RuntimeException("invalid response(s) found");
        }
        return channel.sendTransaction(responses);
    }

    /**
     * Convert a PrivateKey object into a .pem formatted string.
     * @param key
     * @return
     * @throws IOException 
     */
    public static String getPemFromPrivateKey(PrivateKey key) throws IOException {
        StringWriter out = new StringWriter();
        try (JcaPEMWriter writer = new JcaPEMWriter(out)) {
            writer.writeObject(key);
        }
        return out.toString();
    }

    /**
     * Load private key from .pem-formatted file
     * @param privateKeyStr
     * @return Private Key usable
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static PrivateKey loadPrivateKey(String privateKeyStr) throws IOException, GeneralSecurityException {
        PrivateKey key;

        PEMKeyPair bcKeyPair = (PEMKeyPair) new PEMParser(new StringReader(privateKeyStr)).readObject();
        byte[] encoded = bcKeyPair.getPrivateKeyInfo().getEncoded();

        Security.addProvider(new BouncyCastleProvider());
        ASN1InputStream bIn = new ASN1InputStream(new ByteArrayInputStream(encoded));
        PrivateKeyInfo pki = PrivateKeyInfo.getInstance(bIn.readObject());
        String algOid = pki.getPrivateKeyAlgorithm().getAlgorithm().getId();

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance(algOid);
        key = kf.generatePrivate(keySpec);

        return key;
    }

    /**
     * Parse Fabric connection settings from a connection profile JSON
     * @param connectionProfileJson
     * @return 
     */
    public static Map<String, String> parseConnectionProfileJson(String connectionProfileJson) {
        Map<String, String> connectionProfileMap = new HashMap<>();
        try {
            if (connectionProfileJson.contains("\\r\\n")) {
                connectionProfileJson = StringEscapeUtils.unescapeJavaScript(connectionProfileJson);
            }
            JSONObject obj = new JSONObject(connectionProfileJson);        
            
            // get channel
            String channelName = obj.getString("name");
            connectionProfileMap.put("channelName", channelName);
            
            // get cert authority
            JSONObject certAuths = obj.getJSONObject("certificateAuthorities");
            String certAuthKey = (String)certAuths.keys().next();
            JSONObject certAuth = certAuths.getJSONObject(certAuthKey);
            String caUrl = certAuth.getString("url");
            connectionProfileMap.put("caUrl", caUrl);
            if (certAuth.has("tlsCACerts")) {
                String caCert = "";
                Object caCertObj = certAuth.getJSONObject("tlsCACerts").get("pem");
                if (caCertObj instanceof JSONArray) {
                    caCert = ((JSONArray)caCertObj).getString(0);
                } else {
                    caCert = caCertObj.toString();
                }
                connectionProfileMap.put("caCert", caCert);
            }
            
            // get peer
            JSONObject peers = obj.getJSONObject("peers");
            String peerKey = (String)peers.keys().next();
            JSONObject peer = peers.getJSONObject(peerKey);
            String peerUrl = peer.getString("url");
            connectionProfileMap.put("peerName", peerKey);
            connectionProfileMap.put("peerUrl", peerUrl);
            if (peer.has("tlsCACerts")) {
                String peerCert = peer.getJSONObject("tlsCACerts").getString("pem");
                connectionProfileMap.put("peerCert", peerCert);
            }
            
            // get orderer
            if (obj.has("orderers")) {
                JSONObject orderers = obj.getJSONObject("orderers");
                String ordererKey = (String)orderers.keys().next();
                JSONObject orderer = orderers.getJSONObject(ordererKey);
                String ordererUrl = orderer.getString("url");
                connectionProfileMap.put("ordererName", ordererKey);
                connectionProfileMap.put("ordererUrl", ordererUrl);
                if (orderer.has("tlsCACerts")) {
                    String ordererCert = orderer.getJSONObject("tlsCACerts").getString("pem");
                    connectionProfileMap.put("ordererCert", ordererCert);
                }
            }
            
        } catch (Exception e) {
            LogUtil.error(FabricUtil.class.getName(), e, e.getMessage());
        }
        return connectionProfileMap;
    }
    
    /**
     * Convenience method to execute a chaincode query and return results as a JSONArray.
     * @param properties
     * @return 
     */
    public static JSONArray executeQueryFromProperties(Map<String, Object> properties) {
        JSONArray resultArray = new JSONArray();

        // debug mode flag to output additional log messages
        boolean debug = Boolean.valueOf((String) properties.get("debugMode"));

        // get workflow assignment to process hash variables
        WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");

        try {
            // flag to register a new user
            boolean registerNewUser = Boolean.valueOf((String) properties.get("registerNewUser"));

            // user credentials
            String userId = WorkflowUtil.processVariable((String) properties.get("userId"), null, wfAssignment);
            String userCert = WorkflowUtil.processVariable((String) properties.get("userCert"), null, wfAssignment);
            String userPrivateKey = WorkflowUtil.processVariable((String) properties.get("userPrivateKey"), null, wfAssignment);
            String affiliation = WorkflowUtil.processVariable((String) properties.get("affiliation"), null, wfAssignment);
            String mspId = WorkflowUtil.processVariable((String) properties.get("mspId"), null, wfAssignment);

            // admin credentials to register a new user
            String adminId = WorkflowUtil.processVariable((String) properties.get("adminId"), null, wfAssignment);
            String adminSecret = WorkflowUtil.processVariable((String) properties.get("adminSecret"), null, wfAssignment);

            // CA, peer, orderer and channel configuration
            Map<String, String> connectionProfileMap = FabricUtil.parseConnectionProfileJson(WorkflowUtil.processVariable((String) properties.get("connectionProfileJson"), null, wfAssignment));
            String caUrl = WorkflowUtil.processVariable((String) connectionProfileMap.get("caUrl"), null, wfAssignment);
            String caCert = WorkflowUtil.processVariable((String) connectionProfileMap.get("caCert"), null, wfAssignment);
            String peerName = WorkflowUtil.processVariable((String) connectionProfileMap.get("peerName"), null, wfAssignment);
            String peerUrl = WorkflowUtil.processVariable((String) connectionProfileMap.get("peerUrl"), null, wfAssignment);
            String peerCert = WorkflowUtil.processVariable((String) connectionProfileMap.get("peerCert"), null, wfAssignment);
            String ordererName = WorkflowUtil.processVariable((String) connectionProfileMap.get("ordererName"), null, wfAssignment);
            String ordererUrl = WorkflowUtil.processVariable((String) connectionProfileMap.get("ordererUrl"), null, wfAssignment);
            String ordererCert = WorkflowUtil.processVariable((String) connectionProfileMap.get("ordererCert"), null, wfAssignment);
            String channelName = WorkflowUtil.processVariable((String) connectionProfileMap.get("channelName"), null, wfAssignment);

            // chaincode and query settings
            String chaincodeId = WorkflowUtil.processVariable((String) properties.get("chaincodeId"), null, wfAssignment);
            String functionName = WorkflowUtil.processVariable((String) properties.get("functionName"), null, wfAssignment);
            ArrayList<String> argList = new ArrayList<>();
            Object[] paramsValues = (Object[]) properties.get("functionArgs");
            for (Object o : paramsValues) {
                String args;
                if (o instanceof Map) {
                    Map mapping = (HashMap) o;
                    args = mapping.get("functionArgs").toString();
                } else {
                    args = o.toString();
                }
                argList.add(WorkflowUtil.processVariable(args, "", wfAssignment));
            }
            String[] functionArgs = argList.toArray(new String[0]);

            LogUtil.info(FabricUtil.class.getName(), "Invoking Hyperledger Fabric chaincode " + functionName);

            // get user
            FabricUser appUser;
            if (registerNewUser) {
                // create fabric-ca client
                HFCAClient caClient = getHfCaClient(caUrl, caCert);

                // enroll or load admin
                FabricUser admin = getAdmin(caClient, adminId, adminSecret, affiliation, mspId);
                LogUtil.info(FabricUtil.class.getName(), admin.toString());

                // register and enroll new user
                appUser = getUser(caClient, admin, userId, affiliation, mspId);
                LogUtil.info(FabricUtil.class.getName(), appUser.toString());
            } else {
                // get user object based on provided cert and private key
                appUser = getUser(userId, affiliation, mspId, userCert, userPrivateKey);
            }
            if (debug) {
                LogUtil.info(FabricUtil.class.getName(), appUser.toString());
            }

            // get HFC client instance
            HFClient client = getHfClient();
            // set user context
            client.setUserContext(appUser);

            // get HFC channel using the client
            Channel channel = getChannel(client, peerName, peerUrl, peerCert, ordererName, ordererUrl, ordererCert, channelName);
            if (debug) {
                LogUtil.info(FabricUtil.class.getName(), "Channel: " + channel.getName());
            }

            // query blockchain
            String queryResponse = queryBlockChain(client, channelName, chaincodeId, functionName, functionArgs);
            if (debug) {
                LogUtil.info(FabricUtil.class.getName(), "Query Response:" + queryResponse);
            }
            // create JSON object from response
            JSONObject jsonObj;
            if (queryResponse.startsWith("[")) {
                // query response is an array, move it into an object property
                resultArray = new JSONArray(queryResponse);
            } else if (queryResponse.startsWith("{")) {
                jsonObj = new JSONObject(queryResponse);
                resultArray.put(jsonObj);
            }

        } catch (Exception e) {
            LogUtil.error(FabricUtil.class.getName(), e, e.getMessage());
        }

        return resultArray;
    }    

}
