package org.iidp.ostmap.rest_service;

import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.iterators.user.GrepIterator;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

class AccumuloService {
    private static final String PROPERTY_INSTANCE = "accumulo.instance";
    private String accumuloInstanceName;
    private static final String PROPERTY_USER = "accumulo.user";
    private String accumuloUser;
    private static final String PROPERTY_PASSWORD = "accumulo.password";
    private String accumuloPassword;
    private static final String PROPERTY_ZOOKEEPER = "accumulo.zookeeper";
    private String accumuloZookeeper;
    private static final String rawTwitterDataTableName = "RawTwitterData";
    private static final String termIndexTableName = "TermIndex";

    /**
     * Parses the config file at the given path for the necessary parameter.
     *
     * @param path the path to the config file
     * @throws IOException
     */
    void readConfig(String path) throws IOException {
        if(null == path){
            throw new RuntimeException("No path to accumulo config file given. You have to start the webservice with the path to accumulo config as first parameter.");
        }
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(path);
        props.load(fis);
        accumuloInstanceName = props.getProperty(PROPERTY_INSTANCE);
        accumuloUser = props.getProperty(PROPERTY_USER);
        accumuloPassword = props.getProperty(PROPERTY_PASSWORD);
        accumuloZookeeper = props.getProperty(PROPERTY_ZOOKEEPER);
    }

    /**
     * builds a accumulo connector
     *
     * @return the ready to use connector
     * @throws AccumuloSecurityException
     * @throws AccumuloException
     */
    Connector getConnector() throws AccumuloSecurityException, AccumuloException {
        // build the accumulo connector
        Instance inst = new ZooKeeperInstance(accumuloInstanceName, accumuloZookeeper);
        Connector conn = inst.getConnector(accumuloUser, new PasswordToken(accumuloPassword));
        Authorizations auths = new Authorizations("standard");
        conn.securityOperations().changeUserAuthorizations("root", auths);
        return conn;
    }

    /**
     * Creates a scanner for the accumulo term index table.
     *
     * @param token the token to search for
     * @param field the field to search for
     * @return a scanner instance
     * @throws AccumuloSecurityException
     * @throws AccumuloException
     * @throws TableNotFoundException
     */
    Scanner getTermIndexScanner(String token, String field) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        Connector conn = getConnector();
        Authorizations auths = new Authorizations("standard");
        Scanner scan = conn.createScanner(termIndexTableName, auths);
        scan.fetchColumnFamily(new Text(field.getBytes()));
        //Check if the token has a wildcard as last character
        if(hasWildCard(token)){
            token = token.replace("*","");
            scan.setRange(Range.prefix(token));
        }else {
            scan.setRange(new Range(token));
            IteratorSetting grepIterSetting = new IteratorSetting(5, "grepIter", GrepIterator.class);
            GrepIterator.setTerm(grepIterSetting, token);
            scan.addScanIterator(grepIterSetting);
        }
        return scan;
    }

    /**
     * Builds a Range from the given start and end timestamp and returns a batch scanner.
     *
     * @param startTime start time as string
     * @param endTime endt time as string
     * @return the batch scanner
     * @throws AccumuloSecurityException
     * @throws AccumuloException
     * @throws TableNotFoundException
     */
    BatchScanner getRawDataScannerByTimeSpan(String startTime, String endTime) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        Connector conn = getConnector();
        Authorizations auths = new Authorizations("standard");
        BatchScanner scan = conn.createBatchScanner(rawTwitterDataTableName, auths,5);

        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES );
        bb.putLong(Long.parseLong(startTime));

        ByteBuffer bb2 = ByteBuffer.allocate(Long.BYTES); //TODO: make end inclusive again
        bb2.putLong(Long.parseLong(endTime));

        List<Range> rangeList = new ArrayList<>();
        Range r = new Range(new Text(bb.array()), new Text(bb2.array()));
        rangeList.add(r);
        scan.setRanges(rangeList);
        return scan;
    }

    /**
     * Builds a batch scanner for table "RawTwitterData" by the given List of Ranges.
     *
     * @param rangeFilter the list of ranges, applied to the Batch Scanner
     * @return the batch scanner
     * @throws AccumuloSecurityException
     * @throws AccumuloException
     * @throws TableNotFoundException
     */
    BatchScanner getRawDataBatchScanner(List<Range> rangeFilter) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        Connector conn = getConnector();
        Authorizations auths = new Authorizations("standard");
        BatchScanner scan = conn.createBatchScanner(rawTwitterDataTableName, auths,5);
        scan.setRanges(rangeFilter);
        return scan;
    }

    /**
     * Checks if the given string ends with a wildcard *
     *
     * @param token the string to check
     * @return true if ends with wildcard, false if not
     */
    private boolean hasWildCard(String token){
        return token.endsWith("*");
    }
}
