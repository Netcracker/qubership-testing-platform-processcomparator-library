<%@page import="java.nio.file.Files"%>
<%@page import="java.io.File"%>
<%@page contentType="application/json" pageEncoding="UTF-8"
        import="org.qubership.automation.pc.comparator.api.ComparatorResource"%>
<%   
    String actionName = request.getParameter("action");

    String content = request.getParameter("content");

    ComparatorResource cResource = new ComparatorResource();
    out.print(new String(Files.readAllBytes(new File("integration-template/highlighter.json").toPath())));
    
%>
