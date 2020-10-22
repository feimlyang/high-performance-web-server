package com.myserver;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {
    private enum State {
        METHOD,
        URL,
        PROTOCOL,
        PROTOCOL_END,
        HEADER_KEY,
        HEADER_KEY_END,
        HEADER_VALUE,
        HEADER_VALUE_END,
        HEADERS_END,
        BODY
    }

    private State currentState = State.METHOD;
    public String method = "";
    public String url = "";
    public String protocol = "";
    public Map<String, String> headers = new HashMap<String, String>();
    private String currentHeaderKey = "";
    private String currentHeaderValue = "";
    public String body = "";
    private int bodyCounter = 0;

    //CR: \r
    //LF: \n
    //interrupt immediately if not http request
    public boolean parse(String request) throws Exception {
        char[] requestArray = request.toCharArray();
        System.out.print(requestArray);
        for (char ch : requestArray) {
            switch (currentState) {
                case METHOD: {
                    if (ch == ' ') {
                        if (method.equals("GET") || method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE")) {
                            currentState = State.URL;
                        } else new Exception("invalid method");
                    } else method += ch;
                    break;
                }
                case URL: {
                    if (ch == ' ') currentState = State.PROTOCOL;
                    else url += ch;
                    break;
                }
                case PROTOCOL: {
                    if (ch == '\r') currentState = State.PROTOCOL_END;
                    else protocol += ch;
                    break;
                }
                case PROTOCOL_END: {
                    if (ch == '\n') currentState = State.HEADER_KEY;
                    else new Exception("Invalid");
                    break;
                }
                case HEADER_KEY: {
                    if (ch == ':') currentState = State.HEADER_KEY_END;
                    else if (ch == '\r') currentState = State.HEADERS_END;
                    else currentHeaderKey += ch;
                    break;
                }
                case HEADER_KEY_END: {
                    if (ch == ' ') currentState = State.HEADER_VALUE;
                    else new Exception("Invalid");
                    break;
                }
                case HEADER_VALUE: {
                    if (ch == '\r') currentState = State.HEADER_VALUE_END;
                    else currentHeaderValue += ch;
                    break;
                }
                case HEADER_VALUE_END: {
                    if (ch == '\n') {
                        headers.put(currentHeaderKey, currentHeaderValue);
                        currentHeaderKey = currentHeaderValue = "";
                        currentState = State.HEADER_KEY;
                    } else new Exception("Invalid");
                    break;
                }
                case HEADERS_END: {
                    if (ch == '\n') {
                        currentState = State.BODY;
                        if (headers.containsKey("Content-Length")) bodyCounter = Integer.parseInt(headers.get("Content-Length"));
                        else bodyCounter = 0;
                    } else new Exception("Invalid");
                    break;
                }
                case BODY: {
                    if (bodyCounter > 0) {
                        body += ch;
                        --bodyCounter;
                    } else return true;
                    break;
                }
                default:
                    new Exception();
            }
        }
        return false;
    }
}
