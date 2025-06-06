<%@page contentType="application/json" pageEncoding="UTF-8"
        import="org.qubership.automation.pc.comparator.api.ComparatorResource"
        import="org.apache.commons.io.IOUtils"
        %>
<%  response.addHeader("Access-Control-Allow-Origin", "*");
    String actionName = request.getParameter("action");
    String content = request.getParameter("content");
    ComparatorResource cResource = new ComparatorResource();
    if (actionName.equals("compare")) {        
        String result = cResource.compare(content);
    }
%>
