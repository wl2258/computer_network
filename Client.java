package com.company;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client{
    private String user_name;
    private int selected_option;
    private String input_msg;
    private byte[] packet;
    private int len;
    private byte[] input_packet = new byte[1024];
    private Socket client_socket = new Socket("127.0.0.1", 8888);
    private OutputStream os = client_socket.getOutputStream();
    private InputStream is;


    public Client() throws IOException, ParseException {
        init();
        run();
    }

    private void init() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Input user name>> ");
        user_name = br.readLine();

        //TCP 소켓 연결
        is = client_socket.getInputStream();

        System.out.println();
        System.out.println("select eco-message option");
        System.out.println("1. normal eco-message");
        System.out.println("2. eco-message with capital letter");
        System.out.println("3. eco-message with small letter");
        System.out.print(">> ");
        selected_option = Integer.parseInt(br.readLine());
        if (!check_option()) {
            System.out.println("ERROR(NOT APPOINTED!)");
            return;
        }

        System.out.println();
        System.out.print("Input message to eco>> ");
        input_msg = br.readLine();
    }

    private boolean check_option() {
        // 입력된 옵션이 1, 2, 3이 아닌 경우
        if (selected_option != 1 && selected_option != 2 && selected_option != 3){
            return false;
        }
        return true;
    }

    private void makeJsonPacket() throws ParseException {
        //json 형식으로 데이터를 하나의 패킷에 담음
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", selected_option);
        jsonObject.put("message", input_msg);
        jsonObject.put("username", user_name);

        packet = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        len = packet.length;
    }

    private void run() throws IOException {
        try {
            send();
            receive_from_server();
        } catch (IOException | InterruptedException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void send() throws IOException, InterruptedException, ParseException {
        makeJsonPacket();
        os.write(packet);
        os.flush();
    }

    // 서버에서 다시 전송된 결과를 받아옴
    private void receive_from_server() throws IOException, ParseException {

        int inputPacket_len = is.read(input_packet);

        String result_data = new String(input_packet).substring(0, inputPacket_len);

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(result_data);

        String name = String.valueOf(jsonObject.get("username"));
        String msg = String.valueOf(jsonObject.get("message"));

        System.out.println("After: [" + name + "]: " + msg);

    }

}
