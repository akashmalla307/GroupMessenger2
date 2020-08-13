package edu.buffalo.cse.cse486586.groupmessenger2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;
import java.io.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;



/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
//https://stackoverflow.com/questions/29872664/add-key-and-value-into-an-priority-queue-and-sort-by-key-in-java
 class Entry implements Comparable<Entry> {
    private String key;
    private float value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public float getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }


    public Entry(String key, float value) {
        this.key = key;
        this.value = value;
    }

    // getters

    @Override
    public int compareTo(Entry other) {
        return this.getKey().compareTo(other.getKey());
    }
}

//https://www.geeksforgeeks.org/implement-priorityqueue-comparator-java/

class StudentComparator implements Comparator<HashMap<String, String>>{

    @Override
    public int compare(HashMap<String, String> t1, HashMap<String, String> t2) {
        if (Float.valueOf(t1.get("avdproposeseq")) > Float.valueOf(t2.get("avdproposeseq")))
            return 1;
        else if (Float.valueOf(t1.get("avdproposeseq")) < Float.valueOf(t1.get("avdproposeseq")))
            return -1;
        return 0;
    }
}

class StudentComparator1 implements Comparator<Entry>{

    @Override
    public int compare(Entry entry, Entry t1) {
        if (entry.getValue()>t1.getValue())
            return 1;
        else if (entry.getValue()<t1.getValue())
            return -1;
        return 0;
    }
}



public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();



    float[] allproposeseq = new float[]{0,0,0,0,0};
    HashMap<String, String> mssg = new HashMap<String, String>();

    // for comparing the seq of priority queue
    Comparator<HashMap<String, String>> cmp = new Comparator<HashMap<String, String>>() {
        @Override
        public int compare(HashMap<String, String> t1, HashMap<String, String> t2) {

            if (Double.valueOf(t1.get("avdproposeseq")) > Double.valueOf(t2.get("avdproposeseq")))
                return 1;
            else if (Double.valueOf(t1.get("avdproposeseq")) < Double.valueOf(t2.get("avdproposeseq")))
                return -1;
            return 0;
        }
    };

    // priority queue for storing all the mssg(hashmap) in agree seq wise (lowest agree seq will be on th top)
    PriorityQueue<HashMap<String, String> > pq = new
            PriorityQueue<HashMap<String, String>>(25, cmp);

    float proposeseq = 0;
    Integer avdproposeseq=0;
    Integer avdagreeseq=0;

    int avdfifoseq;
    String myportclient;
    int myportc;
    int finalagreeseq =0;
    float max = 0;
    HashMap<String,Integer> portsmap = new HashMap<String, Integer>();


    //private final ContentResolver mContentResolver;
    static final int SERVER_PORT = 10000;
    int seq=0;
    //private final TextView mTextView;
    ContentResolver mContentResolver;
    private final Uri mUri;
    // private final ContentValues[] mContentValues;
    String maxfinalagreeseq;
    Socket[] socketarray = new Socket[5];
    String[] remote_ports;
    public GroupMessengerActivity() {
        //mTextView = _tv;
        //mContentResolver = _cr;
        mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
        //   mContentValues = initTestValues();
        portsmap.put("11108",0);
        portsmap.put("11112",1);
        portsmap.put("11116",2);
        portsmap.put("11120",3);
        portsmap.put("11124",4);
        remote_ports  = new String[]{"11108", "11112", "11116", "11120","11124"};
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        mContentResolver = getContentResolver();

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());


        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        myportclient = myPort;
        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        final EditText editText = (EditText) findViewById(R.id.editText1);
        final Button btn = (Button) findViewById(R.id.button4);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // System.out.println("send button is clicked ");
                Log.v("send button is clicked","");
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                //return true;

            }
        });
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {


        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            System.out.println("serverSocket=" + serverSocket);
            Log.v(TAG,"Inside Server code");
            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             *
             */
            //String message;
            while(true) {
                String ans ="";
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    //socket.setSoTimeout(2500);
                    Log.v(TAG,"Inside Server code");
                    //http://www.jgyan.com/networking/how%20to%20send%20object%20over%20socket%20in%20java.php
                    ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
                    HashMap<String, String> hm1 = (HashMap<String, String>) is.readObject();
                    HashMap<String, String> hm1old = hm1;
                    try {
                        ans = hm1.get("finalagreeseq");
                        maxfinalagreeseq = hm1.get("avdagreeseq");
                        Log.v("value of deliverable", hm1.get("deliverable"));
                        Log.v("value of finalagreeseq", hm1.get("finalagreeseq"));
                        Log.v("value of avdagreeseq", hm1.get("avdagreeseq"));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    // this block for propose seq
                    if(ans.equals("no"))
                    {
                        Log.v("Inside ans is no","{{{{{{{{{{{{{{{{{{{}}}}}}}}}}}}}}}}}}}{{{{{{{{{{{{{{}}}}}}}}}");
                        int portID = Integer.valueOf(hm1.get("remoteport"));
                        //Log.v(TAG,"local avd avdproposeseq and avdagreeseq"+" "+avdproposeseq+" "+ avdagreeseq);
                        proposeseq = (int) (Math.max(avdproposeseq, avdagreeseq) + 1);
                        avdproposeseq = (int)proposeseq;
                        Log.v(TAG,"avdproposeseq = "+avdproposeseq);
                        for (int i = 0; i < 5; i++) {
                            if (remote_ports[i].equals(myportclient)) {
                                myportc = i;
                                break;
                            }
                        }
                        proposeseq = (avdproposeseq + (float) ((myportc + 1) / 10.0));
                        Log.v(TAG,"proposeseq = "+proposeseq);
                        //proposeseq++;

                        Log.v("current avd port and sending back to port with seq with mssg", myportclient + "====" + portID + "====" + proposeseq + "=====" + hm1.get("msgtosend"));
                        hm1.put("avdproposeseq", String.valueOf(proposeseq));
                        // adding the initial propose seq in priority queue and will set with final agree seq later
                        pq.add(hm1);
                        // sending the propose seq to sender(from where we got the request)
                        try {
                            Log.v(TAG, "Sending the reply now");
                            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                            os.writeObject(hm1);
                            os.flush();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    // receiving the final agree seq
                    try {
                        ObjectInputStream isfinal = new ObjectInputStream(socket.getInputStream());
                        try {
                            //java.io.StreamCorruptedException: Wrong format: ac
                            HashMap<String, String> hm4 = (HashMap<String, String>) isfinal.readObject();
                            Log.v(TAG, "inside server received the final agreed msg " + hm4);
                            hm1 = hm4;
                            ans = hm4.get("finalagreeseq");
                        } catch (Exception ex) {
                            Log.v(TAG, "Inside CATCH in server side ..........." + hm1);

                            Log.v(TAG,"we should remove the mssgs of avd "+hm1.get("origin")+" as avd "+hm1.get("origin")+" is not alive");
                            Iterator itr = pq.iterator();
                            Log.v(TAG,"printing the iterator values");
                            while (itr.hasNext()) {
                                //value.remove();
                                HashMap<String, String> hm5= (HashMap<String, String>) itr.next();
                                Log.v(TAG,hm5.get("msgtosend"));
                                if(hm5.get("origin").equals(String.valueOf(hm1.get("origin"))) && hm5.get("deliverable").equals("no")){
                                    Log.v(TAG,"Found 1 element ..................going to remove it");
                                    itr.remove();
                                }
                            }

                            ex.printStackTrace();
                        }
                    }catch(Exception ex){
                        Log.v(TAG,"INSIDE CATCH in server side .........isfinal is broken"+hm1);
                        Log.v(TAG,"we should remove the mssgs of avd "+hm1.get("origin")+" as avd "+hm1.get("origin")+" is not alive");
                        Iterator itr = pq.iterator();
                        Log.v(TAG,"printing the iterator values");
                        while (itr.hasNext()) {
                            //value.remove();
                            HashMap<String, String> hm5= (HashMap<String, String>) itr.next();
                            Log.v(TAG,hm5.get("msgtosend"));
                            if(hm5.get("origin").equals(String.valueOf(hm1.get("origin"))) && hm5.get("deliverable").equals("no")){
                                Log.v(TAG,"Found 1 element ..................going to remove it");
                                itr.remove();
                            }
                        }

                        ex.printStackTrace();
                    }
                    // this block is for agree seq
                    if (ans.equals("yes"))
                    {
                        Log.v(TAG,"Inside ans yes ===============================================================================");
                        Iterator value = pq.iterator();

                        // Displaying the values after iterating through the queue
                        System.out.println("The iterator with old seq .......in server side: ");
                        while (value.hasNext()) {
                            System.out.println(value.next());
                        }

                        float floatavdagreeseq= Float.valueOf(maxfinalagreeseq);
                        avdagreeseq = (int) floatavdagreeseq;
                        Log.v(TAG,"floatavdagreeseq = "+floatavdagreeseq);
                        Log.v(TAG,"avdagreeseq = "+avdagreeseq);
                        Log.v("Going to remove this message with old seq =========",hm1old.get("msgtosend")+"......with max seq "+maxfinalagreeseq);
                        Iterator itr = pq.iterator();
                        Log.v(TAG,"printing the iterator values..........in server side");
                        //removing the msg from priority queue with ols propose seq so that we can add the msg with final agree seq
                        while (itr.hasNext()) {

                            //value.remove();
                            HashMap<String, String> hm5= (HashMap<String, String>) itr.next();
                            Log.v(TAG,hm5.get("msgtosend"));
                            if(hm1old.get("msgtosend").equals(hm5.get("msgtosend"))){
                                Log.v(TAG,"Found 1 element ..................going to remove it");
                                 itr.remove();

                            }
                        }
                        //added the msg with final agree seq
                        pq.add(hm1);

                        Log.v(TAG,"===============================================================================");
                        Log.v(TAG,"values after adding new final agree seq"+maxfinalagreeseq +"for msg "+hm1.get("msgtosend"));
                        Iterator value11 = pq.iterator();
                        System.out.println("The iterator after adding the new values are.......in server side: ");
                        while (value11.hasNext()) {
                            System.out.println(value11.next());
                        }

                        //going to check if it is deliverable and top of the priority queue and going to insert in content provider
                        while (true) {
                            HashMap<String, String> hm5 =null;
                            try {
                                 hm5 = pq.peek();
                                 Log.v(TAG, String.valueOf(hm5));
                                 Log.v(TAG,"the value of peek==="+hm5.get("msgtosend")+" "+hm5.get("deliverable")+" "+hm5.get("avdproposeseq"));
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }
                            if(hm5==null) {
                                Log.v(TAG,"hm5 is NULL..............so going to break");
                                break;
                            }
                            String deliverable = hm5.get("deliverable");
                            if(deliverable.equals("yes") ) {
                                String mssg = hm5.get("msgtosend");
                                Log.v(TAG,"the top element is deliverable"+"...."+hm5.get("msgtosend"));
                                Log.v(TAG,"poll value ========="+pq.poll());
                                publishProgress(mssg);
                            }else{
                                break;
                            }
                        }
                }
                } catch (SocketTimeoutException ste)
                {
                    ste.printStackTrace();
                    Log.v(TAG,"Socket timeout");
                    Log.v(TAG,"the vale of ans ="+ans);
                }catch (Exception e) {
                    // socket.close();
                    e.printStackTrace();
                }
            }
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            //System.out.println("inside onProgressUpdate");
            Log.v(TAG,"Going to insert in ..........CP");
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            ContentValues values = new ContentValues();
            // ContentValues contentValues = new ContentValues();
            String seqstr = String.valueOf(seq);
            values.put("key", seqstr);
            values.put("value", strReceived);

            //gmp.onCreate();
            mContentResolver.insert(mUri,values);
            seq++;
            // gmp.getinfo();

            //  TextView localTextView = (TextView) findViewById(R.id.textView1);
            // localTextView.append("\n");

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */


            return;
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        String msgToSend;


        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(String... msgs) {

            Log.v(TAG, "Inside Client");
            String remotePort;
            Socket socket = null;
            HashMap<String, String> hm2 = new HashMap<String, String>();
            for (int i = 0; i < 5; i++) {
                if (remote_ports[i].equals(myportclient)) {
                    myportc = i;
                    break;
                }
            }

            for (int i = 0; i < 5; i++) {
                remotePort = remote_ports[i];
                System.out.println("sending to remotePort=" + remotePort);
                //creating 5 diff sockets
                try {
                    //for (int i = 0; i < 5; i++) {
                        socketarray[i] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(remote_ports[i]));
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                // setting the timeout for each socket
                try {
                    int timeoutInMs = 25*1000;   // 10 seconds
                    //socketarray[i].setSoTimeout(timeoutInMs);
                }catch(Exception ex){
                    ex.printStackTrace();
                }

                msgToSend = msgs[0];
                Log.v(TAG,"messagetoSend=" + msgToSend+" remoteport="+i+"::"+remotePort+"origin="+myportc);
                // intialising the values in mssg hashmap
                mssg.put("msgtosend", msgToSend);
                mssg.put("avdproposeseq", "0");
                mssg.put("avdagreeseq", "0");
                mssg.put("avdfifoseq", "0");
                mssg.put("remoteport",String.valueOf(i));
                mssg.put("origin",String.valueOf(myportc));
                mssg.put("finalagreeseq","no");
                mssg.put("deliverable","no");
                //Log.v(TAG,"Hashmap is done");
                //sending the msg 1st to all the avd including itself
                try {
                    ObjectOutputStream os;
                    try {
                        //Log.v(TAG," inside outputstream block");
                         os = new ObjectOutputStream(socketarray[i].getOutputStream());
                         os.writeObject(mssg);
                         os.flush();
                         //os.close();
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                  //  socket.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                //receiving the propose sequence no. as a response from all avd including from himself
                ObjectInputStream is = null;
                    try {
                        //Log.v(TAG, " inside inputstream block");
                       // socket.setSoTimeout(5000);
                        is = new ObjectInputStream(socketarray[i].getInputStream());
                        try {
                            //Log.v(TAG, " outside inputstream block");
                            hm2 = (HashMap<String, String>) is.readObject();
                            //is.close();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (SocketTimeoutException ex) {
                            System.out.println("Host not found: " + ex.getMessage());
                        } catch (UnknownHostException ex) {
                            System.out.println("Host not found: " + ex.getMessage());
                        } catch (IOException ioe) {
                            System.out.println("I/O Error " + ioe.getMessage());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                    catch (SocketTimeoutException ste) {
                           ste.printStackTrace();
                           Log.v(TAG,"Socket timeout");
                    }
                    catch(Exception ex){
                        Log.v(TAG,"we should remove the mssgs of avd "+i+" as avd "+i+" is not alive");
                        Iterator itr = pq.iterator();
                        Log.v(TAG,"printing the iterator values");
                        while (itr.hasNext()) {

                            //value.remove();
                            HashMap<String, String> hm5= (HashMap<String, String>) itr.next();
                            Log.v(TAG,hm5.get("msgtosend"));
                            if(hm5.get("origin").equals(String.valueOf(i)) && hm5.get("deliverable").equals("no")){
                                Log.v(TAG,"Found 1 element ..................going to remove it");
                                itr.remove();

                            }
                        }
                              ex.printStackTrace();

                         }
                String proposeseq1 = hm2.get("avdproposeseq");
                Log.v(TAG, "Received the 1st reply from avd with seq"+hm2.get("remoteport")+" "+hm2.get("avdproposeseq")+"  "+hm2.get("msgtosend"));
                try {
                    //Log.v(TAG,"before update allproposeseq"+allproposeseq[Integer.valueOf(hm2.get("remoteport"))]);
                    // storing all the propose seq from all avd
                    allproposeseq[Integer.valueOf(hm2.get("remoteport"))] = Float.valueOf(proposeseq1);
                    Log.v(TAG,"after update allproposeseq"+allproposeseq[Integer.valueOf(hm2.get("remoteport"))]);
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }

            //==============================================================
                    // sorting propose array bec we have to pick the highest propose seq
                    Arrays.sort(allproposeseq);
                    max = allproposeseq[4];
                    // added the final propose seq which will be our agree seq in hashmap
                    HashMap<String, String> hm3 = new HashMap<String, String>();
                     hm3.put("msgtosend", msgToSend);
                     hm3.put("avdfifoseq", "0");
                     hm3.put("avdagreeseq",String.valueOf(max));
                     hm3.put("avdproposeseq",String.valueOf(max));
                     hm3.put("origin",String.valueOf(myportc));
                     hm3.put("finalagreeseq","yes");
                     hm3.put("deliverable","yes");

                 Log.v(TAG,"max seq set by avds "+max+" "+hm3);


            // sending the final agree seq to avd including himself
            for (int i = 0; i < 5; i++) {
                remotePort = remote_ports[i];
                hm3.put("remoteport", remotePort);
                System.out.println("sending to remotePort=" + remotePort);
                    Log.v(TAG,"sending max to all avds with final seq "+hm3.get("avdagreeseq")+" "+hm3.get("finalagreeseq")+" "+hm3.get("msgtosend"));
                    try {
                        ObjectOutputStream os;
                        try {
                            os = new ObjectOutputStream(socketarray[i].getOutputStream());
                            os.writeObject(hm3);
                            os.flush();
                           // os.close();
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }



    }

}
/*
* References-
* https://causeyourestuck.io/2016/01/27/socket-server-client-java
* https://www.javatpoint.com/java-nio-vs-input-output
*https://medium.com/@dilankam/java-serialization-4ff2d5cf5fa8
* https://medium.com/edureka/comparable-in-java-e9cfa7be7ff7
* http://www.javapractices.com/topic/TopicAction.do?Id=8
* https://www.developer.com/java/data/understanding-asynchronous-socket-channels-in-java.html
* https://techtavern.wordpress.com/2010/11/09/how-to-prevent-eofexception-when-socket-is-closed/
* https://stackoverflow.com/questions/15769035/java-net-socketexception-socket-closed-tcp-client-server-communication
* https://www.sitepoint.com/using-androids-content-providers-manage-app-data/
* https://stuff.mit.edu/afs/sipb/project/android/docs/guide/topics/providers/content-provider-basics.html
* https://proandroiddev.com/how-to-test-contentresolver-on-android-38a05e32595e
* https://www.androiddesignpatterns.com/2012/06/content-resolvers-and-content-providers.html
* https://medium.com/@suragch/android-asynctasks-99e501e637e5
* https://medium.com/edureka/socket-programming-in-java-f09b82facd0
* https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-3fc12b29d5fd
* https://stackoverflow.com/questions/2004531/what-is-the-difference-between-socket-and-serversocket
* */
