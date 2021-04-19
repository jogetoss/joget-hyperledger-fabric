package org.joget.hyperledger;

import java.util.HashMap;
import java.util.Map;
import org.joget.plugin.base.PluginManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testApplicationContext.xml"})
public class TestFabric {

    @Autowired 
    PluginManager pluginManager;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test tool to query and update the blockchain.
     */
    @Test
    public void testFabricTool() {
        String pluginFile = "target/joget-hyperledger-fabric.jar";
        String pluginName = "org.joget.hyperledger.FabricTool";
        
        /*-- Test fabcar chaincode functions
        // query all cars
        String functionName = "queryAllCars";
        String[] functionArgs = null;

        // create car
        String functionName = "createCar";
        String[] functionArgs = new String[]{"CAR12", "Honda", "Civic", "White", "Lenny"};

        // query single car
        String functionName = "queryCar";
        String[] functionArgs = new String[]{"CAR12"};        
        --*/

        // set plugin properties
        Map pluginProps = new HashMap();
        pluginProps.put("pluginManager", pluginManager);
        pluginProps.put("userId", "hfuser");
        pluginProps.put("affiliation", "org1");
        pluginProps.put("mspId", "Org1MSP");
        pluginProps.put("registerNewUser", "true");
        pluginProps.put("userCert", "-----BEGIN CERTIFICATE-----\\nMIICcDCCAhegAwIBAgIUECFWHr+mI+mbs0VxfE1/obYKkmkwCgYIKoZIzj0EAwIw\\nczELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNh\\nbiBGcmFuY2lzY28xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMT\\nE2NhLm9yZzEuZXhhbXBsZS5jb20wHhcNMjAwMjI4MDkzMzAwWhcNMjEwMjI3MDkz\\nODAwWjAvMRwwDQYDVQQLEwZjbGllbnQwCwYDVQQLEwRvcmcxMQ8wDQYDVQQDEwZo\\nZnVzZXIwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAR3QvywZLnRtiI1AaxOieFa\\nduck4Wrx7NGXcrLj3RXeWphMip7ntMYUVokC5RIGXKkgYtnTB+PoJt1vpqF6ig7h\\no4HMMIHJMA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBTU\\nWcJISxwlAw/2bTKLgqZSlNQArDArBgNVHSMEJDAigCAfQ/gEM+ExDpoiA/ap/PgJ\\n7cOYbmgDhUQVU5NSlE4POjBdBggqAwQFBgcIAQRReyJhdHRycyI6eyJoZi5BZmZp\\nbGlhdGlvbiI6Im9yZzEiLCJoZi5FbnJvbGxtZW50SUQiOiJoZnVzZXIiLCJoZi5U\\neXBlIjoiY2xpZW50In19MAoGCCqGSM49BAMCA0cAMEQCIFNgmT2Cs2oEkCxMGSWQ\\nn8z+cdCNZcrD477Hh8KkNnxIAiBl8iVRAEM/C7HdbHDqf50qEfUDdwc2pW/vxSps\\nEKw1jw\\u003d\\u003d\\n-----END CERTIFICATE-----\\n");
        pluginProps.put("userPrivateKey", "-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEIHKreHfNagp368HIOGJx0Pwq02ALbAm34GEMXLfyQpN3oAoGCCqGSM49\nAwEHoUQDQgAEd0L8sGS50bYiNQGsTonhWnbnJOFq8ezRl3Ky490V3lqYTIqe57TG\nFFaJAuUSBlypIGLZ0wfj6Cbdb6aheooO4Q==\n-----END EC PRIVATE KEY-----");
        pluginProps.put("adminId", "admin");
        pluginProps.put("adminSecret", "adminpw");
        pluginProps.put("connectionProfileJson", "{\n" +
            "    \"name\": \"mychannel\",\n" +
            "    \"version\": \"1.0.0\",\n" +
            "    \"client\": {\n" +
            "        \"organization\": \"Org1\",\n" +
            "        \"connection\": {\n" +
            "            \"timeout\": {\n" +
            "                \"peer\": {   \n" +
            "                    \"endorser\": \"300\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"organizations\": {\n" +
            "        \"Org1\": {\n" +
            "            \"mspid\": \"Org1MSP\",\n" +
            "            \"peers\": [\n" +
            "                \"peer0.org1.example.com\"\n" +
            "            ],\n" +
            "            \"certificateAuthorities\": [\n" +
            "                \"ca.org1.example.com\"\n" +
            "            ]\n" +
            "        }\n" +
            "    },\n" +
            "    \"peers\": {\n" +
            "        \"peer0.org1.example.com\": {\n" +
            "            \"url\": \"grpcs://peer0.org1.example.com:7051\",\n" +
            "            \"tlsCACerts\": {\n" +
            "                \"pem\": \"-----BEGIN CERTIFICATE-----\\nMIICJzCCAc2gAwIBAgIULYzSCLSBfguGh/39g9KnMb2SA0EwCgYIKoZIzj0EAwIw\\ncDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMQ8wDQYDVQQH\\nEwZEdXJoYW0xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\\nLm9yZzEuZXhhbXBsZS5jb20wHhcNMjAwOTE4MTI1ODAwWhcNMzUwOTE1MTI1ODAw\\nWjBwMQswCQYDVQQGEwJVUzEXMBUGA1UECBMOTm9ydGggQ2Fyb2xpbmExDzANBgNV\\nBAcTBkR1cmhhbTEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEcMBoGA1UEAxMT\\nY2Eub3JnMS5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABGcB\\nzmIRPjC7Wft0rhTnuDFtgzyGX6RA8QiZCY3ar0Ji+ThaGdG7pBMP46baHM2bU2ag\\nyjbLzF6j7IuoSJxCGSijRTBDMA4GA1UdDwEB/wQEAwIBBjASBgNVHRMBAf8ECDAG\\nAQH/AgEBMB0GA1UdDgQWBBRASDPkY0VYU83mc73xJivTRxX7ZjAKBggqhkjOPQQD\\nAgNIADBFAiEArVtSen9t2c5jqbqUz+ObKm7IiHliYOvACsXhIofr5JkCIGo2QOD2\\n5ThMtSKIXJCU8NoO2jxTc8KN+Ll6v6vxkopg\\n-----END CERTIFICATE-----\\n\"\n" +
            "            },\n" +
            "            \"grpcOptions\": {\n" +
            "                \"ssl-target-name-override\": \"peer0.org1.example.com\",\n" +
            "                \"hostnameOverride\": \"peer0.org1.example.com\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"certificateAuthorities\": {\n" +
            "        \"ca.org1.example.com\": {\n" +
            "            \"url\": \"https://org1.example.com:7054\",\n" +
            "            \"caName\": \"ca-org1\",\n" +
            "            \"tlsCACerts\": {\n" +
            "                \"pem\": [\"-----BEGIN CERTIFICATE-----\\nMIICJzCCAc2gAwIBAgIULYzSCLSBfguGh/39g9KnMb2SA0EwCgYIKoZIzj0EAwIw\\ncDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMQ8wDQYDVQQH\\nEwZEdXJoYW0xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\\nLm9yZzEuZXhhbXBsZS5jb20wHhcNMjAwOTE4MTI1ODAwWhcNMzUwOTE1MTI1ODAw\\nWjBwMQswCQYDVQQGEwJVUzEXMBUGA1UECBMOTm9ydGggQ2Fyb2xpbmExDzANBgNV\\nBAcTBkR1cmhhbTEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEcMBoGA1UEAxMT\\nY2Eub3JnMS5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABGcB\\nzmIRPjC7Wft0rhTnuDFtgzyGX6RA8QiZCY3ar0Ji+ThaGdG7pBMP46baHM2bU2ag\\nyjbLzF6j7IuoSJxCGSijRTBDMA4GA1UdDwEB/wQEAwIBBjASBgNVHRMBAf8ECDAG\\nAQH/AgEBMB0GA1UdDgQWBBRASDPkY0VYU83mc73xJivTRxX7ZjAKBggqhkjOPQQD\\nAgNIADBFAiEArVtSen9t2c5jqbqUz+ObKm7IiHliYOvACsXhIofr5JkCIGo2QOD2\\n5ThMtSKIXJCU8NoO2jxTc8KN+Ll6v6vxkopg\\n-----END CERTIFICATE-----\\n\"]\n" +
            "            },\n" +
            "            \"httpOptions\": {\n" +
            "                \"verify\": false\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}");
        pluginProps.put("transactionType", "query");
        pluginProps.put("chaincodeId", "fabcar");
        pluginProps.put("functionName", "queryAllCars");
        pluginProps.put("functionArgs", new String[]{"CAR12", "Honda", "Civic", "White", "Lenny"});        
        pluginProps.put("debugMode", "true");

        // execute plugin
        Boolean status = (Boolean)pluginManager.testPlugin(pluginName, pluginFile, pluginProps, true);
        Assert.assertTrue(status);
    }    
    
}
