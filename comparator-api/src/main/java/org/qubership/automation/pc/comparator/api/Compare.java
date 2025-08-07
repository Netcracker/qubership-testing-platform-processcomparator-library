/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.automation.pc.comparator.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.qubership.automation.pc.comparator.ComparatorManager;
import org.qubership.automation.pc.compareresult.CompareResult;
import org.qubership.automation.pc.configuration.ComparatorConfiguration;
import org.qubership.automation.pc.core.exceptions.ComparatorManagerException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.data.DataPackage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * A servlet that handles HTTP requests for performing data comparison operations.
 *
 * <p>This servlet supports both GET and POST methods and expects a JSON payload
 * containing comparator configuration and data packages. It delegates comparison
 * logic to the {@link ComparatorManager} and returns the result as a JSON response.</p>
 *
 * Expected request parameter: {@code content} - a JSON-formatted string that includes:
 * <ul>
 *   <li>{@code comparatorConfiguration} (optional) - comparison settings</li>
 *   <li>{@code dataPackages} (required) - data to compare</li>
 * </ul>
 *
 * <p>Responses are returned in JSON format and include comparison results or error messages.</p>
 */
public class Compare extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        String content = request.getParameter("content");
        Gson gson = new Gson();
        try (PrintWriter out = response.getWriter()) {
            try {
                JsonObject contextObject = new JsonParser().parse(content).getAsJsonObject();
                ComparatorConfiguration globalConfiguration;
                List<DataPackage> dataPackages = new ArrayList<>();

                //Deserialize contexts            
                if (contextObject.has("comparatorConfiguration")) {
                    globalConfiguration = gson.fromJson(
                            contextObject.getAsJsonObject("comparatorConfiguration").toString(),
                            ComparatorConfiguration.class);
                } else {
                    globalConfiguration = new ComparatorConfiguration();
                }

                if (contextObject.has("dataPackages")) {
                    JsonArray jsonDataPackages = contextObject.getAsJsonArray("dataPackages");
                    for (int i = 0; i < jsonDataPackages.size(); i++) {
                        DataPackage dataPackage = gson.fromJson(jsonDataPackages.get(i).getAsJsonObject().toString(),
                                DataPackage.class);
                        dataPackages.add(dataPackage);
                    }
                    if (!dataPackages.isEmpty()) {
                        ComparatorManager comparatorManager = new ComparatorManager();
                        List<CompareResult> compareResult
                                = comparatorManager.compare(dataPackages, globalConfiguration);
                        out.write(gson.toJson(compareResult));
                    } else {
                        response.setStatus(20101);
                        out.write(JSONUtils.statusMessage(20101, ResponseMessages.msg(20101)).toString());
                    }
                } else {
                    response.setStatus(20101);
                    out.write(JSONUtils.statusMessage(20101, ResponseMessages.msg(20101)).toString());
                }
            } catch (JsonSyntaxException ex) {
                response.setStatus(20102);
                out.write(JSONUtils.statusMessage(20102, ResponseMessages.msg(20102, ex.getMessage())).toString());
            } catch (ComparatorManagerException ex) {
                response.setStatus(ex.getStatusCode());
                out.write(JSONUtils.statusMessage(ex.getStatusCode(),
                        ResponseMessages.msg(ex.getStatusCode(), ex.getMessage())).toString());
            } catch (InterruptedException ex) {
                response.setStatus(20002);
                out.write(JSONUtils.statusMessage(20002,
                        ResponseMessages.msg(20002, ex.getMessage())).toString());
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods.
    // Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    } // </editor-fold>

}
