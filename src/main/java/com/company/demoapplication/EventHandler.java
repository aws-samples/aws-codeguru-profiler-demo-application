package com.shipmentEvents.handlers; 
 
import java.time.Duration; 
import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.List; 
import java.util.Map; 
import java.util.Map.Entry; 
 
import com.amazonaws.regions.Regions; 
import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler; 
import com.amazonaws.services.lambda.runtime.LambdaLogger; 
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent; 
import com.amazonaws.services.s3.AmazonS3; 
import com.amazonaws.services.s3.AmazonS3ClientBuilder; 
import com.amazonaws.services.s3.model.DeleteObjectsRequest; 
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion; 
import com.amazonaws.services.s3.model.ObjectListing; 
import com.amazonaws.services.s3.model.S3ObjectSummary; 
import com.shipmentEvents.util.Constants; 
import java.util.concurrent.ConcurrentHashMap; 
 
 
import org.apache.commons.lang3.tuple.MutablePair; 
import org.apache.commons.lang3.tuple.Pair; 
 
 
public class EventHandler implements RequestHandler<ScheduledEvent, String> { 
 
    /** 
     * Shipment events for a carrier are uploaded to separate S3 buckets based on the source of events. E.g., events originating form 
     * the hand-held scanner are stored in a separate bucket than the ones from mobile App. The Lambda processes events from multiple 
     * sources and updates the latest status of the package in a summary S3 bucket every 15 minutes. 
     *  
     * The events are stored in following format: 
     * - Each status update is a file, where the name of the file is tracking number + random id 
     * - Each file has status and time-stamp as the first 2 lines respectively 
     * - The time at which is file is stored in S3 is not an indication of the time-stamp of the event 
     * - Once the status is marked as DELIVERED, we can stop tracking the package  
     *  
     * A Sample files looks as below: 
     *  FILE-NAME-> '8787323232232332--55322798-dd29-4a04-97f4-93e18feed554' 
     *   >status:IN TRANSIT 
     *   >timestamp: 1573410202 
     *   >Other fields like...tracking history and address 
     */ 
    public String handleRequest(ScheduledEvent scheduledEvent, Context context) { 
 
        final LambdaLogger logger = context.getLogger(); 
        try { 
            processShipmentUpdates(logger); 
            return "SUCCESS"; 
        } catch (final Exception ex) { 
            logger.log(String.format("Failed to process shipment Updates in %s due to %s", scheduledEvent.getAccount(), ex.getMessage())); 
            throw new RuntimeException(ex); 
        } 
    } 
 
 
    private void processShipmentUpdates(final LambdaLogger logger) throws InterruptedException { 
 
        final List<String> bucketsToProcess = Constants.BUCKETS_TO_PROCESS; 
        final ConcurrentHashMap<String, Pair<Long, String>> latestStatusForTrackingNumber = new ConcurrentHashMap<String, Pair<Long, String>>(); 
        final ConcurrentHashMap<String, List<KeyVersion>> filesToDelete = new ConcurrentHashMap<String, List<DeleteObjectsRequest.KeyVersion>>(); 
        bucketsToProcess.parallelStream().forEach(bucketName -> { 
            final List<KeyVersion> filesProcessed = processEventsInBucket(bucketName, logger, latestStatusForTrackingNumber); 
            filesToDelete.put(bucketName, filesProcessed); 
    }); 
         
        final AmazonS3 s3Client = EventHandler.getS3Client(); 
        //Create a new file in the Constants.SUMMARY_BUCKET 
        logger.log("Map of statuses -> " + latestStatusForTrackingNumber); 
        String summaryUpdateName = Long.toString(System.currentTimeMillis()); 
         
        EventHandler.getS3Client().putObject(Constants.SUMMARY_BUCKET, summaryUpdateName, latestStatusForTrackingNumber.toString()); 
         
        long expirationTime = System.currentTimeMillis() + Duration.ofMinutes(1).toMillis(); 
        while(System.currentTimeMillis() < expirationTime) { 
            if (s3Client.doesObjectExist(Constants.SUMMARY_BUCKET, summaryUpdateName)) { 
                break; 
            } 
            logger.log("waiting for file to be created " + summaryUpdateName); 
            Thread.sleep(1000); 
        } 
         
        // Before we delete the shipment updates make sure the summary update file exists 
        if (EventHandler.getS3Client().doesObjectExist(Constants.SUMMARY_BUCKET, summaryUpdateName)) { 
            deleteProcessedFiles(filesToDelete); 
            logger.log("All updates successfully processed"); 
        } else { 
            throw new RuntimeException("Failed to write sumary status, will be retried in 15 minutes"); 
        } 
         
    } 
 
    private List<KeyVersion> processEventsInBucket(String bucketName, LambdaLogger logger, ConcurrentHashMap<String, Pair<Long, String>> latestStatusForTrackingNumber) { 
 
        final AmazonS3 s3Client = EventHandler.getS3Client(); 
        logger.log("Processing Bucket: " + bucketName); 
 
        ObjectListing files = s3Client.listObjects(bucketName); 
        List<KeyVersion> filesProcessed = new ArrayList<DeleteObjectsRequest.KeyVersion>(); 
 
        for (Iterator<?> iterator = files.getObjectSummaries().iterator(); iterator.hasNext(); ) { 
            S3ObjectSummary summary = (S3ObjectSummary) iterator.next(); 
            logger.log("Reading Object: " + summary.getKey()); 
 
            String trackingNumber = summary.getKey().split("--")[0]; 
            Pair<Long, String> lastKnownStatus = latestStatusForTrackingNumber.get(trackingNumber); 
 
            // Check if this shipment has already been delivered, skip this file 
            if (lastKnownStatus != null && "DELIVERED".equals(lastKnownStatus.getRight())) { 
                continue; 
            } 
 
            String fileContents = s3Client.getObjectAsString(bucketName, summary.getKey()); 
 
            if (!isValidFile(fileContents)) { 
                logger.log(String.format("Skipping invalid file %s", summary.getKey())); 
                continue; 
            } 
             
            if (!fileContents.contains("\n")) { 
                 
            } 
            String[] lines = fileContents.split("\n"); 
            String line1 = lines[0]; 
            String line2 = lines[1]; 
 
            String status = line1.split(":")[1]; 
            Long timeStamp = Long.parseLong(line2.split(":")[1]); 
 
 
            if (null == lastKnownStatus || lastKnownStatus.getLeft() < timeStamp) { 
                lastKnownStatus = new MutablePair<Long, String>(timeStamp, status); 
                latestStatusForTrackingNumber.put(trackingNumber, lastKnownStatus); 
            } 
 
            //Add to list of processed files 
            filesProcessed.add(new KeyVersion(summary.getKey())); 
            logger.log("logging Contents of the file" + fileContents); 
        } 
        return filesProcessed; 
    } 
     
 
    private void deleteProcessedFiles(Map<String, List<KeyVersion>> filesToDelete) { 
      final AmazonS3 s3Client = EventHandler.getS3Client(); 
      for (Entry<String, List<KeyVersion>> entry : filesToDelete.entrySet()) { 
          final DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(entry.getKey()).withKeys(entry.getValue()).withQuiet(false); 
          s3Client.deleteObjects(deleteRequest); 
      } 
    } 
     
    private boolean isValidFile(String fileContents) { 
        if (!fileContents.contains("\n")) { 
            return false; 
        } 
        String[] lines = fileContents.split("\n"); 
        for (String l: lines) { 
            if (!l.contains(":")) { 
                return false; 
            } 
        } 
        return true; 
    } 
     
    public static AmazonS3 getS3Client() { 
        return AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build(); 
    } 
     
     
} 
 
