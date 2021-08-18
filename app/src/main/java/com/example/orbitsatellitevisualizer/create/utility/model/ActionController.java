package com.example.orbitsatellitevisualizer.create.utility.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.orbitsatellitevisualizer.R;
import com.example.orbitsatellitevisualizer.connection.LGCommand;
import com.example.orbitsatellitevisualizer.connection.LGConnectionManager;
import com.example.orbitsatellitevisualizer.connection.LGConnectionSendFile;
import com.example.orbitsatellitevisualizer.create.utility.model.balloon.Balloon;
import com.example.orbitsatellitevisualizer.create.utility.model.poi.POI;
import com.example.orbitsatellitevisualizer.create.utility.model.shape.Shape;
import com.neosensory.tlepredictionengine.TlePredictionEngine;

import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.TimeZone;
import java.time.Instant;

/**
 * This class is in charge of sending the commands to liquid galaxy
 */
public class ActionController {

    private static final String TAG_DEBUG = "ActionController";

    private static ActionController instance = null;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Handler handler2 = new Handler(Looper.getMainLooper());

    public synchronized static ActionController getInstance() {
        if (instance == null)
            instance = new ActionController();
        return instance;
    }

    /**
     * Enforce private constructor
     */
    private ActionController() {}

    /**
     * Move the screen to the poi
     *
     * @param poi      The POI that is going to move
     * @param listener The listener of lgcommand
     */
    public void moveToPOI(POI poi, LGCommand.Listener listener) {
        cleanFileKMLs(0);
        sendPoiToLG(poi, listener);
    }

    /**
     * Create the lGCommand to send to the liquid galaxy
     *
     * @param listener The LGCommand listener
     */
    private void sendPoiToLG(POI poi, LGCommand.Listener listener) {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandPOITest(poi), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }


    /**
     * First Clean the KML and then do the orbit
     *
     * @param poi      POI
     * @param listener Listener
     */
    public synchronized void cleanOrbit(POI poi, LGCommand.Listener listener) {
        cleanFileKMLs(0);
        orbit(poi, listener);
    }

    /**
     * Do the orbit
     *
     * @param poi      POI
     * @param listener Listener
     */
    public void orbit(POI poi, LGCommand.Listener listener) {
        LGCommand lgCommandOrbit = new LGCommand(ActionBuildCommandUtility.buildCommandOrbit(poi), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommandOrbit);

        LGCommand lgCommandWriteOrbit = new LGCommand(ActionBuildCommandUtility.buildCommandWriteOrbit(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        lgConnectionManager.addCommandToLG(lgCommandWriteOrbit);

        LGCommand lgCommandStartOrbit = new LGCommand(ActionBuildCommandUtility.buildCommandStartOrbit(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        handler.postDelayed(() -> lgConnectionManager.addCommandToLG(lgCommandStartOrbit), 500);
        cleanFileKMLs(46000);
    }

    public void startOrbit(LGCommand.Listener listener) {

        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        LGCommand lgCommandStartOrbit = new LGCommand(ActionBuildCommandUtility.buildCommandStartOrbit(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        handler.postDelayed(() -> lgConnectionManager.addCommandToLG(lgCommandStartOrbit), 3000);
        System.out.println(lgCommandStartOrbit);
        //cleanFileKMLs(46000);
    }

    /**
     * @param balloon  Balloon with the information to build command
     * @param listener listener
     */
    public void sendBalloon(Balloon balloon, LGCommand.Listener listener) {
        cleanFileKMLs(0);

        Uri imageUri = balloon.getImageUri();
        if (imageUri != null) {
            createResourcesFolder();
            String imagePath = balloon.getImagePath();
            LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
            lgConnectionSendFile.addPath(imagePath);
            lgConnectionSendFile.startConnection();
        }

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandBalloonTest(balloon), LGCommand.CRITICAL_MESSAGE, (String result) -> {
                if (listener != null) {
                    listener.onResponse(result);
                }
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);

            handler.postDelayed(this::writeFileBalloonFile, 500);
        }, 500);
    }

    /**
     * @param balloon  Balloon with the information to build command
     * @param listener listener
     */
    public void sendBalloonTestStoryBoard(Balloon balloon, LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandBalloonTest(balloon), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileBalloonFile, 500);
    }

    /**
     * Send the image of the balloon
     *
     * @param balloon Balloon
     */
    public void sendImageTestStoryboard(Balloon balloon) {
        Uri imageUri = balloon.getImageUri();
        if (imageUri != null) {
            String imagePath = balloon.getImagePath();
            Log.w(TAG_DEBUG, "Image Path: " + imagePath);
            LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
            lgConnectionSendFile.addPath(imagePath);
            lgConnectionSendFile.startConnection();
        }
    }

    /**
     * Paint a balloon with the logos
     */
    public void sendBalloonWithLogos(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getLogosFile(activity);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandBalloonWithLogos(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
            }, 2000);
    }

    public void sendStarlinkfile(AppCompatActivity activity) {
        createResourcesFolder();
        cleanFileKMLs(0);

        String imagePath = getStarlinkFile(activity);
        Log.w(TAG_DEBUG, "STARLINK KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();


        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteStarlinkFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);

        startOrbit(null);
    }

    public void sendISSfile(AppCompatActivity activity) {
        createResourcesFolder();
        cleanFileKMLs(0);

        String imagePath = getISSFile(activity);
        Log.w(TAG_DEBUG, "ISS KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();



        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteISSFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);

        startOrbit(null);
    }

    public void sendEnxanetaFile(AppCompatActivity activity) {
        createResourcesFolder();
        cleanFileKMLs(0);
        String imagePath = getEnxanetaFile(activity);
        Log.w(TAG_DEBUG, "Enxaneta KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteEnxanetaFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);

        startOrbit(null);
    }

    public void sendStarlinkConstFile(AppCompatActivity activity) {
        createResourcesFolder();
        cleanFileKMLs(0);
        String imagePath = getStarlinkConstFile(activity);
        Log.w(TAG_DEBUG, "StarlinkConst KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();


        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteStarlinkConstFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
        startOrbit(null);
    }
    public void sendIridiumConstFile(AppCompatActivity activity) {
        createResourcesFolder();
        cleanFileKMLs(0);
        String imagePath = getIridiumConstFile(activity);
        Log.w(TAG_DEBUG, "IridiumConst KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteIridiumConstFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
        startOrbit(null);
    }

    public void sendRocketTraj(AppCompatActivity activity) {
        createResourcesFolder();
        cleanFileKMLs(0);
        double[] lla_coords = {0,0,0};
        String imagePath = getRocketFile(activity);
        Log.w(TAG_DEBUG, "Rocket KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteRocketFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandInsertFlyTo2(lla_coords),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 4000);

        startOrbit(null);
    }

    private String getLogosFile(AppCompatActivity activity) {
        File file = new File(activity.getCacheDir() + "/logos.png");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("logos.png");
                int size = is.available();
                Log.w(TAG_DEBUG, "SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }
    private String getKMLFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/ISS.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("ISS.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET ISS KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }

    private String getStarlinkFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/Starlink.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("Starlink.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET Starlink KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }

    private String getISSFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/ISS.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("ISS.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET ISS KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }
    private String getEnxanetaFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/Enxaneta.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("Enxaneta.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET Enxaneta KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }
    private String getStarlinkConstFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/StarlinkConst.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("StarlinkConst.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET ISS KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }
    private String getIridiumConstFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/IridiumConst.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("IridiumConst.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET ISS KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }

    private String getRocketFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/rocket_simulation.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("rocket_simulation.kml");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }

    private String readDemoFile(AppCompatActivity activity) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(activity.getAssets().open("demo.txt"), StandardCharsets.UTF_8));

            StringBuilder string = new StringBuilder();
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                string.append(mLine);
            }
            return string.toString();
        } catch (IOException e) {
            Log.w(TAG_DEBUG, "ERROR READING FILE: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG_DEBUG, "ERROR CLOSING: " + e.getMessage());
                }
            }
        }
        return "";
    }


    /**
     * Create the Resource folder
     */
    public void createResourcesFolder() {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandCreateResourcesFolder(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }


    /**
     * Write the shape.kml in the Liquid Galaxy
     */
    private void writeFileShapeFile() {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteShapeFile(),
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }

    private void writeFileISSFile() {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteISSFile(),
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }

    public void sendLiveSCN(AppCompatActivity activity, String scn) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Runnable task1 = () -> {
            try {
                String url = "https://celestrak.com/NORAD/elements/gp.php?CATNR=" + scn + "&FORMAT=TLE";

                String completeTLE[] = Jsoup.connect(url).ignoreContentType(true).execute().body().split("\\n");
                Date date = new Date();
                double[] lla_coords = TlePredictionEngine.getSatellitePosition(completeTLE[1], completeTLE[2], true, date);

                lla_coords[2] = lla_coords[2]*1000;

                /* Inserts the orbit part as a tour */
                String orbit = ActionBuildCommandUtility.buildCommandInsertOrbit(lla_coords, 100000);

                String kml = "echo '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +
                        "    <Document id=\"1\">\n" +
                        "        <Style id=\"4\">\n" +
                        "            <LineStyle id=\"5\">\n" +
                        "                <color>ff0000ff</color>\n" +
                        "                <colorMode>normal</colorMode>\n" +
                        "            </LineStyle>\n" +
                        "            <BalloonStyle>\n" +
                        "                <bgColor>ffffffff</bgColor>\n" +
                        "                <textColor>ffff0000</textColor>\n" +
                        "                <displayMode>default</displayMode>\n" +
                        "            </BalloonStyle>\n" +
                        "        </Style>\n" +
                        "        <Placemark id=\"3\">\n" +
                        "            <name>" + completeTLE[0] + "</name>\n" +
                        "            <description> Satellite Catalog Number: " + scn + " with current coordinates:\nlongitude: " + lla_coords[1] + "\nlatitude: " + lla_coords[0] + "\nheight: " + lla_coords[2] + "</description>\n" +
                        "            <styleUrl>#4</styleUrl>\n" +
                        "            <gx:balloonVisibility>1</gx:balloonVisibility>\n" +
                        "            <Point id=\"2\">\n" +
                        "                <coordinates>" + lla_coords[1] + "," + lla_coords[0] + "," + lla_coords[2] + "</coordinates>\n" +
                        "                <extrude>1</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </Point>\n" +
                        "        </Placemark>\n" + orbit +
                        "    </Document>\n" +
                        "</kml>" +
                        "' > /var/www/html/liveSCN" + scn + ".kml";


                LGCommand lgCommand = new LGCommand(kml, LGCommand.CRITICAL_MESSAGE,(String result) -> {
                });
                LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
                lgConnectionManager.startConnection();
                lgConnectionManager.addCommandToLG(lgCommand);

                writeLiveSCN(scn);

                startOrbit(null);

            } catch (Exception e) {
                System.out.println("ERROR" + e.toString());
            }
        };
        //System.out.println("Submitting the tasks for execution...");
        executorService.submit(task1);
        executorService.shutdown();
    }

    public void sendLiveGroup(AppCompatActivity activity, String group_name) {
        //System.out.println("Inside : " + Thread.currentThread().getName());
        //System.out.println("Creating Executor Service with a thread pool of Size 1");
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Runnable task1 = () -> {
            try {
                String url = "https://celestrak.com/NORAD/elements/gp.php?GROUP=" + group_name + "&FORMAT=TLE";
                System.out.println(url);
                String lines[] = Jsoup.connect(url).ignoreContentType(true).execute().body().split("\\n");
                System.out.println("Num of lines: " + lines.length);
                int i = 0;
                int n = 0;
                int number_of_satellites = (lines.length / 3); //Grouped in three lines each satellite
                System.out.println("Num of satellites: " + number_of_satellites);

                String[][] satellites = new String[number_of_satellites][3];
                Date date = new Date();
                StringBuilder placemarks = new StringBuilder();
                StringBuilder tour = new StringBuilder();


                while (i < 25) {
                    satellites[i][0] = lines[n+0];
                    satellites[i][1] = lines[n+1];
                    satellites[i][2] = lines[n+2];

                    double[] lla_coords = TlePredictionEngine.getSatellitePosition(satellites[i][1], satellites[i][2], true, date);
                    String placemark = "        <Placemark id=\"3\">\n" +
                            "            <name>" + satellites[i][0] + "</name>\n" +
                            "            <description> Current coordinates:\nlongitude: " + lla_coords[1] + "\nlatitude: " + lla_coords[0] + "\nheight: " + lla_coords[2] + "</description>\n" +
                            "            <styleUrl>#4</styleUrl>\n" +
                            "            <gx:balloonVisibility>1</gx:balloonVisibility>\n" +
                            "            <Point id=\"2\">\n" +
                            "                <coordinates>" + lla_coords[1] + "," + lla_coords[0] + "," + lla_coords[2]*1000 + "</coordinates>\n" +
                            "                <extrude>1</extrude>\n" +
                            "                <width>10</width>" +
                            "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                            "            </Point>\n" +
                            "        </Placemark>\n";

                    String flyto = ActionBuildCommandUtility.buildCommandInsertFlyTo(lla_coords);

                    placemarks.append(placemark);
                    tour.append(flyto);
                    System.out.println(placemark);
                    n = n + 3;
                    i++;
                }


                System.out.println("TOUR: " + tour);
                System.out.println("Satellite length: "  + satellites.length);
                System.out.println("SATELLITE TLE 0: "  + satellites[1][0]);
                System.out.println("SATELLITE TLE 1: "  + satellites[1][1]);
                System.out.println("SATELLITE TLE 2: "  + satellites[1][2]);

                /* Inserts the orbit part as a tour */
                String orbit = "";


                String kml = "echo '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +
                        "    <Document id=\"1\">\n" +
                        "        <Style id=\"4\">\n" +
                        "            <LineStyle id=\"5\">\n" +
                        "                <color>ff0000ff</color>\n" +
                        "                <colorMode>normal</colorMode>\n" +
                        "            </LineStyle>\n" +
                        "            <BalloonStyle>\n" +
                        "                <bgColor>ffffffff</bgColor>\n" +
                        "                <textColor>ffff0000</textColor>\n" +
                        "                <displayMode>default</displayMode>\n" +
                        "            </BalloonStyle>\n" +
                        "        </Style>\n" + placemarks + tour +
                        "    </Document>\n" +
                        "</kml>" +
                        "' > /var/www/html/liveSCN" + group_name + ".kml";

                Log.w(TAG_DEBUG, "DEF COMMAND: " + kml.toString());
                LGCommand lgCommand = new LGCommand(kml, LGCommand.CRITICAL_MESSAGE,(String result) -> {
                });
                LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
                lgConnectionManager.startConnection();
                lgConnectionManager.addCommandToLG(lgCommand);

                startOrbit(null);

                writeLiveSCN(group_name);

            } catch (Exception e) {
                System.out.println("ERROR" + e.toString());
            }
        };
        //System.out.println("Submitting the tasks for execution...");
        executorService.submit(task1);
        executorService.shutdown();
    }


    public void sendLiveSCNtest(AppCompatActivity activity, String scn) {
        System.out.println("Inside : " + Thread.currentThread().getName());
        System.out.println("Creating Executor Service with a thread pool of Size 1");
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Runnable task1 = () -> {
            try {
                String url = "https://celestrak.com/NORAD/elements/gp.php?CATNR=" + scn + "&FORMAT=TLE";
                System.out.println(url);
                String completeTLE[] = Jsoup.connect(url).ignoreContentType(true).execute().body().split("\\n");
                Date date = new Date();

                int points = 0;
                int max_points = 60;

                StringBuilder orbit_points = new StringBuilder();
                StringBuilder groundpath_points = new StringBuilder();
                //StringBuilder satellite_poi = new StringBuilder();
                double[] satellite_poi = {0,0,0};
                Date calculedDate;
                while (points <= max_points) {
                    calculedDate = addHoursToJavaUtilDate(date, 2*points);
                    double[] lla_coords_loop = TlePredictionEngine.getSatellitePosition(completeTLE[1], completeTLE[2], true, calculedDate);
                    System.out.println(calculedDate);

                    if (points == 0) {
                        satellite_poi = lla_coords_loop;
                    }

                    orbit_points.append(" " + lla_coords_loop[0] + "," + lla_coords_loop[1] + "," + lla_coords_loop[2]*1000);
                    groundpath_points.append(" " + lla_coords_loop[0] + "," + lla_coords_loop[1] + "," + "0");
                    points++;
                }
                System.out.println("\n\n\n" + orbit_points);
                System.out.println("\n\n\n" + groundpath_points);

                /*
                String kml = "echo '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +
                        "    <Document id=\"1\">\n" +
                        "        <Style id=\"6\">\n" +
                        "            <LineStyle id=\"7\">\n" +
                        "                <color>ff0000ff</color>\n" +
                        "                <colorMode>normal</colorMode>\n" +
                        "            </LineStyle>\n" +
                        "        </Style>\n" +
                        "        <Placemark id=\"3\">\n" +
                        "            <name>25544</name>\n" +
                        "            <LineString id=\"2\">\n" +
                        "                <coordinates>" + orbit_points +"</coordinates>\n" +
                        "                <extrude>0</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </LineString>\n" +
                        "        </Placemark>\n" +
                        "        <Placemark id=\"5\">\n" +
                        "            <name>25544</name>\n" +
                        "            <styleUrl>#6</styleUrl>\n" +
                        "            <LineString id=\"4\">\n" +
                        "                <coordinates>" + groundpath_points +"</coordinates>\n" +
                        "                <extrude>0</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </LineString>\n" +
                        "        </Placemark>\n" +
                        "    </Document>\n" +
                        "</kml>" +
                        "' > /var/www/html/liveSCN" + scn + ".kml";


                String kml = "echo '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +
                        "    <Document id=\"1\">\n" +
                        "        <Style id=\"6\">\n" +
                        "            <LineStyle id=\"7\">\n" +
                        "                <color>ff0000ff</color>\n" +
                        "                <colorMode>normal</colorMode>\n" +
                        "            </LineStyle>\n" +
                        "        </Style>\n" +
                        "        <Placemark id=\"3\">\n" +
                        "            <name>ISS</name>\n" +
                        "            <description>ISS</description>\n" +
                        "            <styleUrl>#4</styleUrl>\n" +
                        "            <gx:balloonVisibility>0</gx:balloonVisibility>\n" +
                        "            <Point id=\"2\">\n" +
                        "                <coordinates>" + satellite_poi[1] + "," + satellite_poi[0] + "," + satellite_poi[2] + "</coordinates>\n" +
                        "                <extrude>0</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </Point>\n" +
                        "        </Placemark>\n" +
                        "        <Placemark id=\"3\">\n" +
                        "            <name>25544</name>\n" +
                        "            <LineString id=\"2\">\n" +
                        "                <coordinates>" + orbit_points +"</coordinates>\n" +
                        "                <extrude>0</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </LineString>\n" +
                        "        </Placemark>\n" +
                        "        <Placemark id=\"5\">\n" +
                        "            <name>25544</name>\n" +
                        "            <styleUrl>#6</styleUrl>\n" +
                        "            <LineString id=\"4\">\n" +
                        "                <coordinates>" + groundpath_points +"</coordinates>\n" +
                        "                <extrude>0</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </LineString>\n" +
                        "        </Placemark>\n" +
                        "    </Document>\n" +
                        "</kml>\n" +
                        "' > /var/www/html/liveSCN" + scn + ".kml";
                */

                String kml = "echo '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +
                        "    <Document id=\"1\">\n" +
                        "        <Style id=\"4\">\n" +
                        "            <LineStyle id=\"5\">\n" +
                        "                <color>ff0000ff</color>\n" +
                        "                <colorMode>normal</colorMode>\n" +
                        "            </LineStyle>\n" +
                        "            <BalloonStyle>\n" +
                        "                <bgColor>ffffffff</bgColor>\n" +
                        "                <textColor>ffff0000</textColor>\n" +
                        "                <displayMode>default</displayMode>\n" +
                        "            </BalloonStyle>\n" +
                        "        </Style>\n" +
                        "        <Style id=\"9\">\n" +
                        "            <LineStyle id=\"10\">\n" +
                        "                <color>ffffffff</color>\n" +
                        "                <colorMode>normal</colorMode>\n" +
                        "                <width>2</width>\n" +
                        "            </LineStyle>\n" +
                        "        </Style>\n" +
                        "        <Style id=\"13\">\n" +
                        "            <LineStyle id=\"14\">\n" +
                        "                <color>c80000ff</color>\n" +
                        "                <colorMode>normal</colorMode>\n" +
                        "            </LineStyle>\n" +
                        "        </Style>\n" +
                        "        <Placemark id=\"3\">\n" +
                        "            <name>ISS</name>\n" +
                        "            <description>ISS</description>\n" +
                        "            <styleUrl>#4</styleUrl>\n" +
                        "            <gx:balloonVisibility>0</gx:balloonVisibility>\n" +
                        "            <Point id=\"2\">\n" +
                        "                <coordinates>" + satellite_poi[0] + "," + satellite_poi[1] + "," + satellite_poi[2] + "</coordinates>\n" +
                        "                <extrude>1</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </Point>\n" +
                        "        </Placemark>\n" +
                        "        <Placemark id=\"3\">\n" +
                        "            <name>ISS</name>\n" +
                        "            <description>ISS</description>\n" +
                        "            <styleUrl>#4</styleUrl>\n" +
                        "            <gx:balloonVisibility>0</gx:balloonVisibility>\n" +
                        "            <Point id=\"2\">\n" +
                        "                <coordinates>" + satellite_poi[0] + "," + satellite_poi[1] + "," + "0" + "</coordinates>\n" +
                        "                <extrude>0</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </Point>\n" +
                        "        </Placemark>\n" +
                        "    </Document>\n" +
                        "</kml>\n" +
                        "' > /var/www/html/liveSCN" + scn + ".kml";


                Log.w(TAG_DEBUG, "DEF COMMAND: " + kml.toString());
                System.out.println("<coordinates>" + orbit_points +"</coordinates>\n");
                LGCommand lgCommand = new LGCommand(kml, LGCommand.CRITICAL_MESSAGE,(String result) -> {
                });
                LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
                lgConnectionManager.startConnection();
                lgConnectionManager.addCommandToLG(lgCommand);

                // orbitLLA(lla_coords, null);

                writeLiveSCN(scn);

            } catch (Exception e) {
                System.out.println("ERROR" + e.toString());
            }
        };
        System.out.println("Submitting the tasks for execution...");
        executorService.submit(task1);
        executorService.shutdown();
    }




    /**
     * Send the command to liquid galaxy
     *
     * @param listener listener

    public void sendShape(Shape shape, LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendShape(), LGCommand.CRITICAL_MESSAGE, (String result) -> { //Should be buildCommandSendShape(shape)
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }
    */
    /*
    public void sendPOI(LGCommand.Listener listener, POI poi) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendShape(POI poi), LGCommand.CRITICAL_MESSAGE, (String result)) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }
    */




    public void sendStarlink(LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendStarlink(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }

    public void sendISS(LGCommand.Listener listener) {
        cleanFileKMLs(0);
        /*
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendISS(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
        */
        handler.postDelayed(this::writeFileShapeFile, 500);
    }
    /*
    public void sendEnxaneta(LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendEnxaneta(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }

    public void sendStarlinkConst(LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendStarlinkConst(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }

    public void sendIridiumConst(LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendIridiumConst(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }*/

    /**
     * It cleans the kmls.txt file
     */
    public void cleanFileKMLs(int duration) {
        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCleanKMLs(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, duration);
        System.out.println("CLEAN KML");
        //cleanQuery(duration);
        exitTour();
    }

    /**
     * It cleans the kmls.txt file
     */
    public void cleanQuery(int duration) {
        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCleanQuery(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, duration);
    }


    /**
     * Send both command to the Liquid Galaxy
     *
     * @param poi     Poi with the location information
     * @param balloon Balloon with the information to paint the balloon
     */
    public void TourGDG(POI poi, Balloon balloon) {
        cleanFileKMLs(0);
        sendBalloonTourGDG(balloon, null);
        sendPoiToLG(poi, null);
    }

    /**
     * Send a balloon in the case of the tour
     *
     * @param balloon  Balloon with the information to build command
     * @param listener listener
     */
    private void sendBalloonTourGDG(Balloon balloon, LGCommand.Listener listener) {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandBalloonTest(balloon), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileBalloonFile, 1000);
    }

    /**
     * Write the file of the balloon
     */
    private void writeFileBalloonFile() {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteBalloonFile(),
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }

    private void writeLiveEnxaneta() {
        String command = "echo 'http://lg1:81/liveEnxaneta.kml' > " +
                "/var/www/html/" +
                "kmls.txt";
        Log.w(TAG_DEBUG, "command: " + command);
        LGCommand lgCommand = new LGCommand(command,
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }

    private void writeLiveISS() {
        String command = "echo 'http://lg1:81/liveISS.kml' > " +
                "/var/www/html/" +
                "kmls.txt";
        Log.w(TAG_DEBUG, "command: " + command);
        LGCommand lgCommand = new LGCommand(command,
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }

    private void writeLiveStarlink() {
        String command = "echo 'http://lg1:81/liveStarlink.kml' > " +
                "/var/www/html/" +
                "kmls.txt";
        Log.w(TAG_DEBUG, "command: " + command);
        LGCommand lgCommand = new LGCommand(command,
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }

    public void writeLiveSCN(String scn) {
        String command = "echo 'http://localhost:81/liveSCN" + scn + ".kml' > " +
                "/var/www/html/" +
                "kmls.txt";
        Log.w(TAG_DEBUG, "command: " + command);
        LGCommand lgCommand = new LGCommand(command,
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }

    public void sendSpaceportFile(AppCompatActivity activity, String description, String name, double[] lla_coords, String imagePath) {

        createResourcesFolder();
        //String imageName = getSpaceportFile(activity, imagePath);
        //Log.w(TAG_DEBUG, "Spaceport image file: " + imageName);
        cleanFileKMLs(0);
        /* Inserts the orbit part as a tour */
        String orbit = ActionBuildCommandUtility.buildCommandInsertOrbit(lla_coords, 1000);

        String kml = "echo '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>" + name + "</name>\n" +
                "\t<StyleMap id=\"m_ylw-pushpin\">\n" +
                "\t\t<Pair>\n" +
                "\t\t\t<key>normal</key>\n" +
                "\t\t\t<styleUrl>#s_ylw-pushpin</styleUrl>\n" +
                "\t\t</Pair>\n" +
                "\t\t<Pair>\n" +
                "\t\t\t<key>highlight</key>\n" +
                "\t\t\t<styleUrl>#s_ylw-pushpin_hl</styleUrl>\n" +
                "\t\t</Pair>\n" +
                "\t</StyleMap>\n" +
                "\t<Style id=\"s_ylw-pushpin_hl\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>1.4</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/track.png</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t\t<hotSpot x=\"32\" y=\"32\" xunits=\"pixels\" yunits=\"pixels\"/>\n" +
                "\t\t</IconStyle>\n" +
                "\t\t<ListStyle>\n" +
                "\t\t</ListStyle>\n" +
                "\t</Style>\n" +
                "\t<Style id=\"s_ylw-pushpin\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>1.2</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/track.png</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t\t<hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/>\n" +
                "\t\t</IconStyle>\n" +
                "\t\t<ListStyle>\n" +
                "\t\t</ListStyle>\n" +
                "\t</Style>\n" +
                "\t<Placemark>\n" +
                "\t\t<name>" + name + "</name>\n" +
                "\t\t<description>\n" + description + "</description>\n" +
                "\t\t<LookAt>\n" +
                "\t\t\t<longitude>" + lla_coords[1] + "</longitude>\n" +
                "\t\t\t<latitude>" + lla_coords[0] + "</latitude>\n" +
                "\t\t\t<altitude>" + lla_coords[2] + "</altitude>\n" +
                "\t\t\t<heading>-0.001127248273239458</heading>\n" +
                "\t\t\t<tilt>5.841915356537878</tilt>\n" +
                "\t\t\t<range>4793.403883588249</range>\n" +
                "\t\t\t<gx:altitudeMode>relativeToGround</gx:altitudeMode>\n" +
                "\t\t</LookAt>\n" +
                "\t\t<styleUrl>#m_ylw-pushpin</styleUrl>\n" +
                "\t\t<gx:balloonVisibility>1</gx:balloonVisibility>\n" +
                "\t\t<Point>\n" +
                "\t\t\t<gx:drawOrder>1</gx:drawOrder>\n" +
                "\t\t\t<coordinates>" + lla_coords[0] + "," + lla_coords[1] + "," + lla_coords[2] + "</coordinates>\n" +
                "\t\t</Point>\n" +
                "\t</Placemark>\n" + orbit +
                "</Document>\n" +
                "</kml>"+
                "' > /var/www/html/spaceport" + name.split(" ")[0] + ".kml";

        System.out.println(kml);
        System.out.println(name.split(" ")[0]);

        LGCommand lgCommand = new LGCommand(kml, LGCommand.CRITICAL_MESSAGE,(String result) -> {
        });
        System.out.println(lgCommand);
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        startOrbit(null);

        writeSpaceport(name.split(" ")[0]);
    }

    public void writeSpaceport(String name) {
        String command = "echo 'http://localhost:81/spaceport" + name + ".kml' > " +
                "/var/www/html/" +
                "kmls.txt";
        Log.w(TAG_DEBUG, "command: " + command);
        LGCommand lgCommand = new LGCommand(command,
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }

    private String getSpaceportFile(AppCompatActivity activity, String imageName) {
        System.out.println("ImageName: " + imageName);
        File file = new File(activity.getFilesDir() + imageName);
        System.out.println("File exist? : " + file.exists());
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open(imageName);
                //InputStream is = bitmapToInputStream(drawableToBitmap(activity.getDrawable(R.drawable.china_sp)));
                int size = is.available();
                Log.w(TAG_DEBUG, "GET SPACEPORT IMAGE SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR GET SPACEPORT FILE: " + e.getMessage());
            }
        }
        String imagePath = file.getPath();
        System.out.println("FINAL IMAGE PATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        return imageName;
    }





    /**
     * Send the tour kml
     * @param actions Storyboard's actions
     * @param listener Listener
     */
    public void sendTour(List<Action> actions, LGCommand.Listener listener){
        cleanFileKMLs(0);
        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandTour(actions), LGCommand.CRITICAL_MESSAGE, (String result) -> {
                if (listener != null) {
                    listener.onResponse(result);
                }
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);

            LGCommand lgCommandWriteTour = new LGCommand(ActionBuildCommandUtility.buildCommandwriteStartTourFile(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
                if (listener != null) {
                    listener.onResponse(result);
                }
            });
            lgConnectionManager.addCommandToLG(lgCommandWriteTour);

            LGCommand lgCommandStartTour = new LGCommand(ActionBuildCommandUtility.buildCommandStartTour(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            handler2.postDelayed(() -> lgConnectionManager.addCommandToLG(lgCommandStartTour), 1500);
        }, 1000);
    }


    /**
     * Exit Tour
     */
    public void exitTour(){
        //cleanFileKMLs(0);
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandExitTour(),
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        LGCommand lgCommandCleanSlaves = new LGCommand(ActionBuildCommandUtility.buildCommandCleanSlaves(),
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        lgConnectionManager.addCommandToLG(lgCommandCleanSlaves);
    };

    public Date addHoursToJavaUtilDate(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //calendar.add(Calendar.HOUR_OF_DAY, hours);
        calendar.add(Calendar.MINUTE, hours);
        return calendar.getTime();
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static InputStream bitmapToInputStream(Bitmap bitmap) {
        int size = bitmap.getHeight() * bitmap.getRowBytes();
        ByteBuffer buffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(buffer);
        return new ByteArrayInputStream(buffer.array());
    }

}
