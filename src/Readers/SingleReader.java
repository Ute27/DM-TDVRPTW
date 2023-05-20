package Readers;

import Parameters.Customer;
import Parameters.Position;
import Parameters.ProblemInstance;
import Parameters.Td_info;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SingleReader {

    List<String> fileNames = Arrays.asList("R101_25.json", "R101_50.json", "R101_100.json",
            "R201_25.json", "R201_50.json", "R201_100.json",
            "C101_25.json", "C101_50.json", "C101_100.json",
            "C201_25.json", "C201_50.json", "C201_100.json",
            "RC101_25.json", "RC101_50.json", "RC101_100.json",
            "RC201_25.json", "RC201_50.json", "RC201_100.json");

    public ProblemInstance readThisInstance(String filepath) {

        System.out.println("Reading " + filepath);
        try {
            // Read file into a string
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();

            // Parse JSON string
            JSONObject json = new JSONObject(stringBuilder.toString());

            // Access different properties from the JSON object
            JSONObject digraph = json.getJSONObject("digraph");
            int vertexCount = digraph.getInt("vertex_count");
            int arcCount = digraph.getInt("arc_count");
            JSONArray arcs = digraph.getJSONArray("arcs");
            JSONArray coordinates = digraph.getJSONArray("coordinates");

            int start_depot = json.getInt("start_depot");
            int end_depot = json.getInt("end_depot");
            JSONArray distances = json.getJSONArray("distances");
            int capacity = json.getInt("capacity");
            JSONArray demands = json.getJSONArray("demands");
            JSONArray service_times = json.getJSONArray("service_times");
            JSONArray time_windows = json.getJSONArray("time_windows");
            JSONArray horizon = json.getJSONArray("horizon");
            int speed_zone_count = json.getInt("speed_zone_count");
            JSONArray speed_zones = json.getJSONArray("speed_zones");
            int cluster_count = json.getInt("cluster_count");
            JSONArray clusters = json.getJSONArray("clusters");
            JSONArray cluster_speeds = json.getJSONArray("cluster_speeds");
            //skip profits voorlopig


            int nbPeriods = speed_zone_count;
            int nbCategories = cluster_count;
            int[][] arcCategories = new int[vertexCount][vertexCount];
            for (int i = 0; i < vertexCount; i++) {
                JSONArray categoryArray = clusters.getJSONArray(i);
                for (int j = 0; j < vertexCount; j++) {
                    arcCategories[i][j] = categoryArray.getInt(j);
                }
            }

            double[][] travelSpeedsPC = new double[cluster_count][speed_zone_count];
            double[][] travelSpeedsPB = new double[speed_zone_count][cluster_count];
            for (int i = 0; i < cluster_count; i++) {
                JSONArray speedsArray = cluster_speeds.getJSONArray(i);
                for (int j = 0; j < speed_zone_count; j++) {
                    travelSpeedsPC[i][j] = speedsArray.getDouble(j);
                }
            }
            for (int i = 0; i < speed_zone_count; i++) {
                for (int j = 0; j < cluster_count; j++) {
                    travelSpeedsPB[i][j] = travelSpeedsPC[j][i];
                }
            }


            double[][] speedzones = new double[speed_zone_count][2];
            for (int i = 0; i < speed_zone_count; i++) {
                JSONArray bpointArray = speed_zones.getJSONArray(i);
                for (int j = 0; j < 2; j++) {
                    speedzones[i][j] = bpointArray.getDouble(j);
                }
            }

            List<Double> bpoints = new ArrayList<>();
            for (double[] speedzone : speedzones) {
                bpoints.add(speedzone[0]);
            }

            Td_info tdi = new Td_info(nbPeriods,nbCategories,vertexCount,bpoints,travelSpeedsPB,arcCategories);

            List<Customer> customers = new ArrayList<>();
            for(int customer=0; customer<vertexCount; customer++) {

                double x = coordinates.getJSONArray(customer).getDouble(0);
                double y = coordinates.getJSONArray(customer).getDouble(1);
                double demand = demands.getDouble(customer);
                double readyTime = time_windows.getJSONArray(customer).getDouble(0);
                double dueTime = time_windows.getJSONArray(customer).getDouble(1);
                double serviceTime = service_times.getDouble(customer);
                boolean startDepot = customer == start_depot;

                Customer customerToAdd = new Customer(customer,new Position(x,y),demand, readyTime,dueTime,serviceTime,startDepot);
                customers.add(customerToAdd);
            }

            double[][] distancesToGive = new double[vertexCount][vertexCount];
            for (int i = 0; i < vertexCount; i++) {
                JSONArray distanceArray = distances.getJSONArray(i);
                for (int j = 0; j < vertexCount; j++) {
                    distancesToGive[i][j] = distanceArray.getDouble(j);
                }
            }
            String[] path = filepath.split("/");
            String name = path[path.length-1];
            String[] filejson = name.split("\\.");
            String filename = filejson[0];

            ProblemInstance pi = new ProblemInstance(filename,customers,distancesToGive,capacity,tdi);

            System.out.println("Reading successful.");
            System.out.println("This instance has the following tdi: " + tdi);
            return pi;

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Reading not successful.");

        return null;
    }



}

