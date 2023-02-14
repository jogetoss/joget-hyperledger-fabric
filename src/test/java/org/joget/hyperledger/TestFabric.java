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
//        pluginProps.put("userCert", "-----BEGIN CERTIFICATE-----\\nMIICYTCCAgigAwIBAgIUfYlEi84jTJFrP0HXufrI/EDSAM8wCgYIKoZIzj0EAwIw\\ncDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMQ8wDQYDVQQH\\nEwZEdXJoYW0xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\\nLm9yZzEuZXhhbXBsZS5jb20wHhcNMjMwMjEzMDMwOTAwWhcNMjQwMjEzMDMxNDAw\\nWjAvMRwwDQYDVQQLEwZjbGllbnQwCwYDVQQLEwRvcmcxMQ8wDQYDVQQDEwZoZnVz\\nZXIwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAS0lrw7IMu2a0XwYS75FDxyKzf/\\n0201x48A/+wan2lxG3Z/Bq8lfOY5FpttzHqFS1xErlLZIDRq/7K/uabfF1/0o4HA\\nMIG9MA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBRtdoQL\\n49vyZi7W+kGV7qaYaw+7jjAfBgNVHSMEGDAWgBTjgx5dDIN8+6y7u13tLnYdR5i6\\nXTBdBggqAwQFBgcIAQRReyJhdHRycyI6eyJoZi5BZmZpbGlhdGlvbiI6Im9yZzEi\\nLCJoZi5FbnJvbGxtZW50SUQiOiJoZnVzZXIiLCJoZi5UeXBlIjoiY2xpZW50In19\\nMAoGCCqGSM49BAMCA0cAMEQCICuru6ItIH54zntX9OCspi57dLL9e5nMKwGGoez7\\na1ZAAiBnbpIOgmBbzzsSfdvcVT4ldA8g9A60To6eAK/mVQY3Hg\\u003d\\u003d\\n-----END CERTIFICATE-----\\n");
//        pluginProps.put("userPrivateKey", "-----BEGIN EC PRIVATE KEY-----\n"
//                + "MHcCAQEEIKzam2zbn4IuIvjPMTIbYrRAQYk3Jlxd7kyEFLK6iuVtoAoGCCqGSM49\n"
//                + "AwEHoUQDQgAEtJa8OyDLtmtF8GEu+RQ8cis3/9NtNcePAP/sGp9pcRt2fwavJXzm\n"
//                + "ORabbcx6hUtcRK5S2SA0av+yv7mm3xdf9A==\n"
//                + "-----END EC PRIVATE KEY-----");
        pluginProps.put("adminId", "admin");
        pluginProps.put("adminSecret", "adminpw");
        pluginProps.put("connectionProfileJson", "{\n"
                + "    \"name\": \"mychannel\",\n"
                + "    \"version\": \"1.0.0\",\n"
                + "    \"client\": {\n"
                + "        \"organization\": \"Org1\",\n"
                + "        \"connection\": {\n"
                + "            \"timeout\": {\n"
                + "                \"peer\": {\n"
                + "                    \"endorser\": \"300\"\n"
                + "                }\n"
                + "            }\n"
                + "        }\n"
                + "    },\n"
                + "    \"organizations\": {\n"
                + "        \"Org1\": {\n"
                + "            \"mspid\": \"Org1MSP\",\n"
                + "            \"peers\": [\n"
                + "                \"peer0.org1.example.com\"\n"
                + "            ],\n"
                + "            \"certificateAuthorities\": [\n"
                + "                \"ca.org1.example.com\"\n"
                + "            ]\n"
                + "        }\n"
                + "    },\n"
                + "    \"peers\": {\n"
                + "        \"peer0.org1.example.com\": {\n"
                + "            \"url\": \"grpcs://peer0.org1.example.com:7051\",\n"
                + "            \"tlsCACerts\": {\n"
                + "                \"pem\": \"-----BEGIN CERTIFICATE-----\\nMIICJjCCAc2gAwIBAgIUEGRTd3ZIDtXqqbQ/XJOmdy9fcJAwCgYIKoZIzj0EAwIw\\ncDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMQ8wDQYDVQQH\\nEwZEdXJoYW0xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\\nLm9yZzEuZXhhbXBsZS5jb20wHhcNMjMwMjEzMTEzMDAwWhcNMzgwMjA5MTEzMDAw\\nWjBwMQswCQYDVQQGEwJVUzEXMBUGA1UECBMOTm9ydGggQ2Fyb2xpbmExDzANBgNV\\nBAcTBkR1cmhhbTEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEcMBoGA1UEAxMT\\nY2Eub3JnMS5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABEdI\\nCkDZkqDV45aVlIHRbWfO9/p2bopEi/HOQnOXcC/L8VMQc1PF5bgfKg2MTYl6uDCq\\n1koVGZTauJym0Xtkb0mjRTBDMA4GA1UdDwEB/wQEAwIBBjASBgNVHRMBAf8ECDAG\\nAQH/AgEBMB0GA1UdDgQWBBTaijbrEekWZv3cNXpCmJESHZ/OzjAKBggqhkjOPQQD\\nAgNHADBEAiAEUe+JkWZbq7ynd0m7Xq41FzePQn+b2nrQyrZK87YrwAIgUdouZY61\\n+iHDZFn2VEmF6sgORMA19EaYsqei/l1KLuc=\\n-----END CERTIFICATE-----\\n\"\n"
                + "            },\n"
                + "            \"grpcOptions\": {\n"
                + "                \"ssl-target-name-override\": \"peer0.org1.example.com\",\n"
                + "                \"hostnameOverride\": \"peer0.org1.example.com\"\n"
                + "            }\n"
                + "        }\n"
                + "    },\n"
                + "    \"certificateAuthorities\": {\n"
                + "        \"ca.org1.example.com\": {\n"
                + "            \"url\": \"https://org1.example.com:7054\",\n"
                + "            \"caName\": \"ca-org1\",\n"
                + "            \"tlsCACerts\": {\n"
                + "                \"pem\": [\"-----BEGIN CERTIFICATE-----\\nMIICJjCCAc2gAwIBAgIUEGRTd3ZIDtXqqbQ/XJOmdy9fcJAwCgYIKoZIzj0EAwIw\\ncDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMQ8wDQYDVQQH\\nEwZEdXJoYW0xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\\nLm9yZzEuZXhhbXBsZS5jb20wHhcNMjMwMjEzMTEzMDAwWhcNMzgwMjA5MTEzMDAw\\nWjBwMQswCQYDVQQGEwJVUzEXMBUGA1UECBMOTm9ydGggQ2Fyb2xpbmExDzANBgNV\\nBAcTBkR1cmhhbTEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEcMBoGA1UEAxMT\\nY2Eub3JnMS5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABEdI\\nCkDZkqDV45aVlIHRbWfO9/p2bopEi/HOQnOXcC/L8VMQc1PF5bgfKg2MTYl6uDCq\\n1koVGZTauJym0Xtkb0mjRTBDMA4GA1UdDwEB/wQEAwIBBjASBgNVHRMBAf8ECDAG\\nAQH/AgEBMB0GA1UdDgQWBBTaijbrEekWZv3cNXpCmJESHZ/OzjAKBggqhkjOPQQD\\nAgNHADBEAiAEUe+JkWZbq7ynd0m7Xq41FzePQn+b2nrQyrZK87YrwAIgUdouZY61\\n+iHDZFn2VEmF6sgORMA19EaYsqei/l1KLuc=\\n-----END CERTIFICATE-----\\n\"]\n"
                + "            },\n"
                + "            \"httpOptions\": {\n"
                + "                \"verify\": false\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}");
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
