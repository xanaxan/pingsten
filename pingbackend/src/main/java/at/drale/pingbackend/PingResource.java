package at.drale.pingbackend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.io.File;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

@Path("/api/ping2")
public class PingResource {

    // Store progress per taskId
    private static final Map<String, Integer> progressMap = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> resultMap = new ConcurrentHashMap<>();

    public static class Entry {
        public String name;
        public String ipWork;
        public String ipVpn;
    }

    @POST
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> startPingMission() {
        String taskId = UUID.randomUUID().toString();
        progressMap.put(taskId, 0);

        // Start the ping task asynchronously
        new Thread(() -> {
            Map<String, String> results = getPingResults(taskId);
            resultMap.put(taskId, results);
        }).start();

        return Map.of("taskId", taskId);
    }

    //ss


    @GET
    @Path("/progress/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Integer> getProgress(@PathParam("taskId") String taskId) {
        return Map.of("progress", progressMap.getOrDefault(taskId, 0));
    }

    @GET
    @Path("/result/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getResult(@PathParam("taskId") String taskId) {
        return resultMap.getOrDefault(taskId, Map.of());
    }

    // The time-consuming task
    public Map<String, String> getPingResults(String taskId) {
        // Map<String, Map<String, Boolean>> results = new HashMap<>();
        Map<String, String> results = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Entry> entries = mapper.readValue(
                new File("output.json"),
                new TypeReference<List<Entry>>() {}
            );

            int total = entries.size();
            int processed = 0;

            for (Entry entry : entries) {
                String status = "none";
                if (entry.ipWork != null && !entry.ipWork.isEmpty()) {
                    status = isReachable(entry.ipWork) ? "work" : status;
                }
                if (entry.ipVpn != null && !entry.ipVpn.isEmpty()) {
                    status = isReachable(entry.ipVpn) ? "home" : status;
                }
                results.put(entry.name, status);

                processed++;
                int percent = (int) ((processed * 100.0) / total);
                progressMap.put(taskId, percent);
            }
            progressMap.put(taskId, 100); // Ensure 100% at the end
        } catch (Exception e) {
            progressMap.put(taskId, 100);
            throw new RuntimeException("Failed to process output.json", e);
        }
        return results;
    }

    private boolean isReachable(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isReachable(3000);
        } catch (Exception e) {
            return false;
        }
    }
}
