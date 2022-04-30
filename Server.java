package com.company;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Server {

    private Socket server_socket;
    private String user_name;
    private int selected_option;
    private String input_msg;
    private byte[] packet = new byte[1024];
    private byte[] outPacket;
    private int len;
    private OutputStream os;
    private InputStream is;

    public Server() throws IOException {
        run();
    }

    private void run() throws IOException {
        ServerSocket server = new ServerSocket(8888);
        try {
            while(true) {
                server_socket = server.accept();
                os = server_socket.getOutputStream();
                receive();
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void receive() throws IOException, ParseException {

        is = server_socket.getInputStream();
        len = is.read(packet);

        //packet에 저장된 데이터 파싱
        String data = new String(packet).substring(0, len);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(data);

        input_msg = String.valueOf(jsonObject.get("message"));
        String option = String.valueOf(jsonObject.get("option"));
        selected_option = Integer.parseInt(option);
        String before_msg = input_msg;

        //에코 옵션에 따른 처리
        if (selected_option == 2) {
            input_msg = input_msg.toUpperCase(Locale.ROOT);
        }
        else if (selected_option == 3) {
            input_msg = input_msg.toLowerCase(Locale.ROOT);
        }

        user_name = (String) jsonObject.get("username");
        print_result(before_msg);

        if (len > 0)
            re_send_to_client();
    }


    private void print_result(String before_msg) {
        System.out.println("Before: [" + user_name + "]: " + before_msg);
        System.out.println("After: [" + user_name + "]: " + input_msg);
    }


    //에코 옵션에 따른 처리 후의 메시지를 다시 클라리언트에게 보내주기 위한 패킷
    private void makeJsonPacket() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", selected_option);
        jsonObject.put("username", user_name);
        jsonObject.put("message", input_msg);

        outPacket = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }


   private void re_send_to_client() throws IOException{
        makeJsonPacket();
        os.write(outPacket);
        os.flush();
   }

}
