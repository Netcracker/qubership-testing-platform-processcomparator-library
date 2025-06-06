<%@page contentType="application/json" pageEncoding="UTF-8"
        import="org.qubership.automation.pc.reader.api.ReaderResource"%>
<%   
    String actionName = request.getParameter("action");
    String content = request.getParameter("content");

    ReaderResource rResource = new ReaderResource();
    try {
        if(actionName.equals("read")) {
            out.print(rResource.read(content));
        } else if(actionName.equals("read/testConnection")) {
            out.print(rResource.testConnection(content));
        }
    } catch( Exception ex) {
        ex.printStackTrace();
        throw new Exception(ex);
    }
    
%>
