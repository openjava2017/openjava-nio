package com.openjava.nio.endpoint;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openjava.nio.util.Constants;
import com.openjava.nio.util.StringUtils;

/**
 * Hash consistent algorithm for cluster management
 */
public class EndPointBucket
{
    private static Logger logger = LoggerFactory.getLogger(EndPointBucket.class);
    
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    // avoid recurring construction
    private static ThreadLocal<MessageDigest> MD5 = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance( "MD5" );
            } catch (NoSuchAlgorithmException nae) {
                throw new IllegalStateException( "no md5 algorythm found");            
            }
        }
    };
    
    private TreeMap<Long, SocketAddress> consistentBuckets = new TreeMap<Long, SocketAddress>();
    
    /**
     * endPointList:  HOST:PORT:WEIGHT(192.168.1.154:9091:1)
     * if no weight provided, 1 by default
     */
    public Set<SocketAddress> refreshConsistentBuckets(List<String> endPointList)
    {
        Lock writeLock = lock.writeLock();
        
        if (endPointList == null || endPointList.isEmpty()) {
            writeLock.lock();
            consistentBuckets.clear();
            writeLock.unlock();
            
            return Collections.emptySet();
        }
        
        int totalWeight = 0;
        int[] weights = new int[endPointList.size()];
        String[] hosts = new String[endPointList.size()];
        int[] ports = new int[endPointList.size()];
        
        for (int i = 0; i < endPointList.size(); i ++) {
            String endPointAddress = endPointList.get(i);
            if (StringUtils.isBlank(endPointAddress)) {
                logger.error("Illegal endpoint address: " + endPointAddress);
                continue;
            }
            
            String[] addressInfo = endPointAddress.split("" + Constants.COLON_CHAR);
            if (addressInfo.length < 2) {
                logger.error("Illegal endpoint address: " + endPointAddress);
                continue;
            }
            
            hosts[i] = addressInfo[0];
            if (StringUtils.isBlank(addressInfo[0])) {
                logger.error("Illegal endpoint address: " + endPointAddress);
            }
            
            try {
                ports[i] = Integer.parseInt(addressInfo[1]);
            } catch (NumberFormatException nfe) {
                ports[i] = 0;
                logger.error("Illegal endpoint address: " + endPointAddress);
                continue;
            }
            
            weights[i] = 1;
            if (addressInfo.length > 2) {
                try {
                    weights[i] = Integer.parseInt(addressInfo[2]);
                } catch (NumberFormatException nfe) {
                    logger.error("Illegal endpoint address: " + endPointAddress);
                    continue;
                }
            }
            totalWeight += weights[i];
        }

        MessageDigest md5 = MD5.get();
        Set<SocketAddress> addresses = new HashSet<SocketAddress>();
        
        writeLock.lock();
        consistentBuckets.clear();
        try {
            for (int i = 0; i < hosts.length; i ++ ) {
                if (StringUtils.isBlank(hosts[i]) || ports[i] <= 0) {
                    continue;
                }
                
                double factor = Math.floor(((double)(40 * hosts.length * weights[i])) / (double) totalWeight);
                String hostInfo = hosts[i] + Constants.COLON_CHAR + ports[i];
                SocketAddress address = new InetSocketAddress(hosts[i], ports[i]);
                addresses.add(address);
                for (long j = 0; j < factor; j++) {
                    try {
                        byte[] d = md5.digest((hostInfo + "-" + j).getBytes("UTF-8"));
                        for (int h = 0; h < 4; h ++) {
                            Long k = ((long)(d[3 + h * 4] & 0xFF) << 24) | ((long)(d[2 + h * 4] & 0xFF) << 16)
                                | ((long)(d[1 + h * 4] & 0xFF) << 8) | ((long)(d[0 + h * 4] & 0xFF));
                            consistentBuckets.put(k, address);
                        }
                    } catch (UnsupportedEncodingException e) {
                        // Never happen for UTF-8
                    }
                                
                }
            }
        } finally {
            writeLock.unlock();
        }
        
        return addresses;
    }
    
    public SocketAddress getBucket(String key)
    {
        Lock readLock = lock.readLock();
        readLock.lock();
        
        try {
            // Get hash using hash consistent algorithm
            MessageDigest md5 = MD5.get();
            md5.reset();
            md5.update(key.getBytes("UTF-8"));
            byte[] bKey = md5.digest();
            long hash = ((long)(bKey[3]&0xFF) << 24) | ((long)(bKey[2]&0xFF) << 16) | ((long)(bKey[1]&0xFF) << 8) | (long)(bKey[0]&0xFF);
            
            Long bucket = this.consistentBuckets.ceilingKey(hash);
            bucket = (bucket == null) ? this.consistentBuckets.firstKey() : bucket;
            return consistentBuckets.get(bucket);
        } catch (UnsupportedEncodingException e) {
            // Never happen for UTF-8
            return null;
        } catch (NoSuchElementException nse) {
            return null;
        } finally {
            readLock.unlock();
        }
    }
    
    public static void main(String[] args)
    {
        List<String> endPointList = new ArrayList<String>();
        endPointList.add("192.168.1.192:9091:1");
        endPointList.add("192.168.1.192:9092:1");
        endPointList.add("192.168.1.192:9093:1");
        endPointList.add("192.168.1.192:9094:1");
        endPointList.add("192.168.1.192:9095:1");
        
        EndPointBucket cluster = new EndPointBucket();
        cluster.refreshConsistentBuckets(endPointList);
        
        List<String> keyList = new ArrayList<String>();
        for (int i=0; i<20; i++) {
            keyList.add(UUID.randomUUID().toString());
        }
        
//        long start = System.currentTimeMillis();
        for (String key : keyList) {
            System.out.println(cluster.getBucket(key));
        }
//        System.out.println(System.currentTimeMillis() - start);
        System.out.println("-----------------------------------------");
        endPointList.add("192.168.1.192:9096:2");
        cluster.refreshConsistentBuckets(endPointList);
        for (String key : keyList) {
            System.out.println(cluster.getBucket(key));
        }
    }
}
