package com.example.orbitsatellitevisualizer.create.utility.model;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.orbitsatellitevisualizer.connection.LGCommand;
import com.example.orbitsatellitevisualizer.connection.LGConnectionManager;
import com.example.orbitsatellitevisualizer.connection.LGConnectionSendFile;
import com.example.orbitsatellitevisualizer.create.utility.model.balloon.Balloon;
import com.example.orbitsatellitevisualizer.create.utility.model.poi.POI;
import com.example.orbitsatellitevisualizer.create.utility.model.shape.Shape;
import com.neosensory.tlepredictionengine.TlePredictionEngine;

import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public void orbitLLA(double[] lla_coords, LGCommand.Listener listener) {
        LGCommand lgCommandOrbit = new LGCommand(ActionBuildCommandUtility.buildCommandOrbitLLA(lla_coords), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommandOrbit);
        System.out.println(lgCommandOrbit);

        LGCommand lgCommandWriteOrbit = new LGCommand(ActionBuildCommandUtility.buildCommandWriteOrbit(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        lgConnectionManager.addCommandToLG(lgCommandWriteOrbit);
        System.out.println(lgCommandWriteOrbit);


        LGCommand lgCommandStartOrbit = new LGCommand(ActionBuildCommandUtility.buildCommandStartOrbit(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        handler.postDelayed(() -> lgConnectionManager.addCommandToLG(lgCommandStartOrbit), 500);
        System.out.println(lgCommandStartOrbit);
        cleanFileKMLs(46000);
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

        String imagePath = getStarlinkFile(activity);
        Log.w(TAG_DEBUG, "ISS KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteStarlinkFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
    }

    public void sendISSfile(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getISSFile(activity);
        Log.w(TAG_DEBUG, "ISS KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteISSFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
    }

    public void sendEnxanetaFile(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getEnxanetaFile(activity);
        Log.w(TAG_DEBUG, "Enxaneta KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteEnxanetaFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
    }

    public void sendStarlinkConstFile(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getStarlinkConstFile(activity);
        Log.w(TAG_DEBUG, "StarlinkConst KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteStarlinkConstFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
    }
    public void sendIridiumConstFile(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getIridiumConstFile(activity);
        Log.w(TAG_DEBUG, "IridiumConst KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteIridiumConstFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
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

    public void sendLiveEnxaneta(AppCompatActivity activity) {
        System.out.println("Inside : " + Thread.currentThread().getName());
        System.out.println("Creating Executor Service with a thread pool of Size 4");
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Runnable task1 = () -> {
            try {
                String completeTLE[] = Jsoup.connect("https://celestrak.com/NORAD/elements/gp.php?CATNR=47954&FORMAT=TLE").ignoreContentType(true).execute().body().split("\\n");
                Date date = new Date();
                int offset = ((date.getTimezoneOffset())/60);
                System.out.println("Offset: " + offset);
                Date utc_date = addHoursToJavaUtilDate(date,offset);

                //TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

                double[] lla_coords = TlePredictionEngine.getSatellitePosition(completeTLE[1], completeTLE[2], true, utc_date);
                lla_coords[2] = lla_coords[2]*1000;

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
                        "            <description>ISS</description>\n" +
                        "            <styleUrl>#4</styleUrl>\n" +
                        "            <gx:balloonVisibility>0</gx:balloonVisibility>\n" +
                        "            <Point id=\"2\">\n" +
                        "                <coordinates>" + lla_coords[1] + "," + lla_coords[0] + "," + lla_coords[2] + "</coordinates>\n" +
                        "                <extrude>1</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </Point>\n" +
                        "        </Placemark>\n" +
                        "    </Document>\n" +
                        "</kml>" +
                        "' > /var/www/html/liveEnxaneta.kml";

                Log.w(TAG_DEBUG, "DEF COMMAND: " + kml.toString());
                orbitLLA(lla_coords, null);
                LGCommand lgCommand = new LGCommand(kml, LGCommand.CRITICAL_MESSAGE,(String result) -> {
                });
                LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
                lgConnectionManager.startConnection();
                lgConnectionManager.addCommandToLG(lgCommand);

                handler.postDelayed(this::writeLiveEnxaneta, 500);

            } catch (Exception e) {
                System.out.println("ERROR" + e.toString());
            }
        };

        System.out.println("Submitting the tasks for execution...");
        executorService.submit(task1);
        executorService.shutdown();
    }

    public void sendLiveISS(AppCompatActivity activity) {
        System.out.println("Inside : " + Thread.currentThread().getName());
        System.out.println("Creating Executor Service with a thread pool of Size 4");
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Runnable task1 = () -> {
            try {
                String completeTLE[] = Jsoup.connect("https://celestrak.com/NORAD/elements/gp.php?CATNR=25544&FORMAT=TLE").ignoreContentType(true).execute().body().split("\\n");
                Date date = new Date();

                double[] lla_coords = TlePredictionEngine.getSatellitePosition(completeTLE[1], completeTLE[2], true);

                lla_coords[2] = lla_coords[2]*1000;

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
                        "            <description>ISS</description>\n" +
                        "            <styleUrl>#4</styleUrl>\n" +
                        "            <gx:balloonVisibility>0</gx:balloonVisibility>\n" +
                        "            <Point id=\"2\">\n" +
                        "                <coordinates>" + lla_coords[1] + "," + lla_coords[0] + "," + lla_coords[2] + "</coordinates>\n" +
                        "                <extrude>1</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </Point>\n" +
                        "        </Placemark>\n" +
                        "    </Document>\n" +
                        "</kml>" +
                        "' > /var/www/html/liveISS.kml";

                Log.w(TAG_DEBUG, "DEF COMMAND: " + kml.toString());
                orbitLLA(lla_coords, null);
                LGCommand lgCommand = new LGCommand(kml, LGCommand.CRITICAL_MESSAGE,(String result) -> {
                });
                LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
                lgConnectionManager.startConnection();
                lgConnectionManager.addCommandToLG(lgCommand);

                handler.postDelayed(this::writeLiveISS, 500);

            } catch (Exception e) {
                System.out.println("ERROR" + e.toString());
            }
        };

        System.out.println("Submitting the tasks for execution...");
        executorService.submit(task1);
        executorService.shutdown();
    }

    public void sendLiveStarlink(AppCompatActivity activity) {
        System.out.println("Inside : " + Thread.currentThread().getName());
        System.out.println("Creating Executor Service with a thread pool of Size 4");
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Runnable task1 = () -> {
            try {
                String completeTLE[] = Jsoup.connect("https://celestrak.com/NORAD/elements/gp.php?CATNR=47413&FORMAT=TLE").ignoreContentType(true).execute().body().split("\\n");
                Date date = new Date();
                double[] lla_coords = TlePredictionEngine.getSatellitePosition(completeTLE[1], completeTLE[2], true, date);

                lla_coords[2] = lla_coords[2]*1000;

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
                        "            <description>ISS</description>\n" +
                        "            <styleUrl>#4</styleUrl>\n" +
                        "            <gx:balloonVisibility>1</gx:balloonVisibility>\n" +
                        "            <Point id=\"2\">\n" +
                        "                <coordinates>" + lla_coords[1] + "," + lla_coords[0] + "," + lla_coords[2] + "</coordinates>\n" +
                        "                <extrude>1</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </Point>\n" +
                        "        </Placemark>\n" +
                        "    </Document>\n" +
                        "</kml>" +
                        "' > /var/www/html/liveStarlink.kml";

                Log.w(TAG_DEBUG, "DEF COMMAND: " + kml.toString());
                orbitLLA(lla_coords, null);
                LGCommand lgCommand = new LGCommand(kml, LGCommand.CRITICAL_MESSAGE,(String result) -> {
                });
                LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
                lgConnectionManager.startConnection();
                lgConnectionManager.addCommandToLG(lgCommand);

                handler.postDelayed(this::writeLiveStarlink, 500);

            } catch (Exception e) {
                System.out.println("ERROR" + e.toString());
            }
        };

        System.out.println("Submitting the tasks for execution...");
        executorService.submit(task1);
        executorService.shutdown();
    }


    public void sendLiveSCN(AppCompatActivity activity, String scn) {
        //System.out.println("Inside : " + Thread.currentThread().getName());
        //System.out.println("Creating Executor Service with a thread pool of Size 1");
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Runnable task1 = () -> {
            try {
                String url = "https://celestrak.com/NORAD/elements/gp.php?CATNR=" + scn + "&FORMAT=TLE";
                //System.out.println(url);
                String completeTLE[] = Jsoup.connect(url).ignoreContentType(true).execute().body().split("\\n");
                Date date = new Date();
                double[] lla_coords = TlePredictionEngine.getSatellitePosition(completeTLE[1], completeTLE[2], true, date);

                lla_coords[2] = lla_coords[2]*1000;

                //Testing
                int points = 0;
                int max_points = 36;
                StringBuilder string_points = new StringBuilder();
                StringBuilder orbit_points = new StringBuilder();

                while (points <= max_points) {
                    double[] lla_coords_loop = TlePredictionEngine.getSatellitePosition(completeTLE[1], completeTLE[2], true, addHoursToJavaUtilDate(date, 4*points));
                    string_points.append(" " + lla_coords_loop[0] + "," + lla_coords_loop[1] + "," + lla_coords_loop[2]);
                    orbit_points.append(" " + lla_coords_loop[0] + "," + lla_coords_loop[1] + "," + lla_coords_loop[2]*1000);
                    points++;
                }
                //System.out.println("\n\n\n" + string_points);
                //System.out.println("\n\n\n" + orbit_points);

                /*
                //FUNCIONA SINGLE SATELLITE
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
                        "        </Placemark>\n" +
                        "    </Document>\n" +
                        "</kml>" +
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
                        "        </Placemark>\n" +
                        /* ORBITES RARES
                        "        <Placemark id=\"5\">\n" +
                        "            <name>25544</name>\n" +
                        "            <styleUrl>#6</styleUrl>\n" +
                        "            <LineString id=\"4\">\n" +
                        "                <coordinates>" + orbit_points +"</coordinates>\n" +
                        "                <extrude>0</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </LineString>\n" +
                        "        </Placemark>\n" +
                         */
                        "    </Document>\n" +
                        "</kml>" +
                        "' > /var/www/html/liveSCN" + scn + ".kml";

                //Log.w(TAG_DEBUG, "DEF COMMAND: " + kml.toString());
                //cleanFileKMLs(0);
                //Thread.sleep(400);
                LGCommand lgCommand = new LGCommand(kml, LGCommand.CRITICAL_MESSAGE,(String result) -> {
                });
                LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
                lgConnectionManager.startConnection();
                lgConnectionManager.addCommandToLG(lgCommand);

                orbitLLA(lla_coords, null);

                writeLiveSCN(scn);

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
                while (i < number_of_satellites) {
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
                    placemarks.append(placemark);
                    System.out.println(placemark);
                    n = n + 3;
                    i++;
                }

                System.out.println(placemarks);
                System.out.println("Satellite length: "  + satellites.length);
                System.out.println("SATELLITE TLE 0: "  + satellites[1][0]);
                System.out.println("SATELLITE TLE 1: "  + satellites[1][1]);
                System.out.println("SATELLITE TLE 2: "  + satellites[1][2]);


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
                        "        </Style>\n" + placemarks +
                        /* ORBITES RARES
                        "        <Placemark id=\"5\">\n" +
                        "            <name>25544</name>\n" +
                        "            <styleUrl>#6</styleUrl>\n" +
                        "            <LineString id=\"4\">\n" +
                        "                <coordinates>" + orbit_points +"</coordinates>\n" +
                        "                <extrude>0</extrude>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "            </LineString>\n" +
                        "        </Placemark>\n" +
                         */
                        "    </Document>\n" +
                        "</kml>" +
                        "' > /var/www/html/liveSCN" + group_name + ".kml";

                Log.w(TAG_DEBUG, "DEF COMMAND: " + kml.toString());
                LGCommand lgCommand = new LGCommand(kml, LGCommand.CRITICAL_MESSAGE,(String result) -> {
                });
                LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
                lgConnectionManager.startConnection();
                lgConnectionManager.addCommandToLG(lgCommand);

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
     * @param shape    Shape with the information to build the command
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
        String command = "echo 'http://lg1:81/liveSCN" + scn + ".kml' > " +
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
        cleanFileKMLs(0);
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

}
