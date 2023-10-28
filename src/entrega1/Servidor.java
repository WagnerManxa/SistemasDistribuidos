package entrega1;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

public class Servidor {
    private JTextArea logTextArea;
    private JTextArea userListTextArea;
    private JSONObject[] cadastros;
    private int portNumber; 
    private JSONObject userTokens[];

    public Servidor() {
    	cadastros = new JSONObject[50]; //vetor de clientes
    	
        cadastros[0] = new JSONObject();
        cadastros[0].put("name", "admin");
        cadastros[0].put("email", "admin@admin.com");
        cadastros[0].put("type", "admin");
        cadastros[0].put("password", hashearSenha("E00CF25AD42683B3DF678C61F42C6BDA"));  //admin1   
        
        cadastros[1] = new JSONObject();
        cadastros[1].put("name", "user");
        cadastros[1].put("email", "user@user.com");
        cadastros[1].put("type", "user");
        cadastros[1].put("password", hashearSenha("E00CF25AD42683B3DF678C61F42C6BDA"));  //admin1  

        
    	userTokens = new JSONObject[50]; // vetor de tokens
       

        JFrame frame = new JFrame("Servidor Sistemas Distribuidos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700,300);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField portField = new JTextField(5);
        portField.setText(String.valueOf(portNumber)); // Define a porta padrão
        JButton saveButton = new JButton("Salvar Porta");
        JButton clearButton = new JButton("Limpar Log"); // Botão para limpar o log
        JButton tokensButton = new JButton("Mostrar Tokens");
        controlPanel.add(new JLabel("Porta:"));
        controlPanel.add(portField);
        controlPanel.add(saveButton);
        controlPanel.add(clearButton);
        controlPanel.add(tokensButton);

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        
        userListTextArea = new JTextArea();
        userListTextArea.append("Usuarios conectados:"+"\n");
        userListTextArea.setEditable(false); 
        JScrollPane userListScrollPane = new JScrollPane(userListTextArea);
        userListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Adicione uma barra de rolagem vertical
        

        JScrollPane scrollPane = new JScrollPane(logTextArea);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, userListScrollPane);
        frame.add(controlPanel, BorderLayout.SOUTH);
 
        splitPane.setResizeWeight(0.8); 
        frame.add(splitPane, BorderLayout.CENTER);

        frame.setVisible(true);

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int newPort = Integer.parseInt(portField.getText());
                    if (newPort > 0 && newPort < 65536) { // Verifica se a porta está dentro dos limites válidos
                        portNumber = newPort;
                        log("Porta salva: " + portNumber);
                        Thread serverThread = new Thread(new ServerListener(portNumber));
                        serverThread.start();
                    } else {
                        log("Porta inválida. A porta deve estar entre 1 e 65535.");
                    }
                } catch (NumberFormatException ex) {
                    log("Porta inválida. Certifique-se de que seja um número válido.");
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logTextArea.setText(""); // Limpa o conteúdo da área de log
            }
        });

        tokensButton.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e){                
                listarTokens();
            }
        });
    
}
   

    
    private int encontrarPosicaoVaziaCadastros() {
        for (int i = 0; i < cadastros.length; i++) {
            if (cadastros[i] == null) {
                return i; // Retorna a primeira posição vazia encontrada
            }
        }
        return -1; // Retorna -1 se o vetor estiver cheio
    }
    
    private int encontrarPosicaoVaziaTokens() {
        for (int i = 0; i < userTokens.length; i++) {
            if (userTokens[i] == null) {
                return i; // Retorna a primeira posição vazia encontrada
            }
        }
        return -1; // Retorna -1 se o vetor estiver cheio
    }
    
    private void listarTokens(){
        log("Tokens no servidor: ");
        for (int i = 0; i < userTokens.length; i++) {            
          if (userTokens[i] != null){
            log(userTokens[i].toString());
        }else{
            log("Fim!");
            return;
            }
        }
    }
     
    private boolean validarLogin(String usuario, String senha) {
    	    for (JSONObject cadastro : cadastros) {
    	        if (cadastro != null && usuario.equals(cadastro.optString("email", ""))
    	                && BCrypt.checkpw(senha, cadastro.optString("password", ""))) {
    	            return true; // Usuário e senha válidos
    	        }
    	    }
    	    return false; // Usuário e senha não encontrados
    	}
    
    private int buscarId(String usuario) {
    	int id = 0;
        for (int i = 0; i < cadastros.length; i++) {
            JSONObject cadastro = cadastros[i];
            if (cadastro != null && usuario.equals(cadastro.optString("email", ""))) {
                id= i; 
            }
        }
        return id;
    }
    
    public boolean isAdmin(String usuario) {
    	 for (JSONObject cadastro : cadastros) {
             if (cadastro != null && usuario.equals(cadastro.optString("email", ""))
                     && ("admin").equals(cadastro.optString("type", ""))) {
                 return true; // Usuário e senha válidos
             }
         }
         return false;
    }
    
    private String buscarEmail(int userId) {    	   
        JSONObject cadastro = cadastros[userId];
        if (cadastro != null ) 
            return cadastro.optString("email", "");
        else 
        	return "";
    }

    private void addUserToken(String token, InetAddress ip){
    	int posicao = encontrarPosicaoVaziaTokens(); 
    	String email = buscarEmail(Integer.parseInt(JwtUtil.getUserIdFromToken(token)));
	     JSONObject novo = new JSONObject();
	     novo.put("usuarioId", JwtUtil.getUserIdFromToken(token));
	     novo.put("token", token);
	     novo.put("email", email);
	     novo.put("ip", ip);
	     userTokens[posicao] = novo;   
	     userListTextArea.append(email+ip+ "\n");
   }
    
    private void deleteUserToken(String token) {

		for (int i = 0; i < userTokens.length; i++) {
            if (userTokens[i] != null && userTokens[i].optString("usuarioId","").equals(JwtUtil.getUserIdFromToken(token))) {
                String text = userListTextArea.getText();
                String newText = text.replaceAll(userTokens[i].optString("email", "") +userTokens[i].optString("ip", "") + "\n", ""); // Remova o usuário da lista
                userListTextArea.setText(newText);
            	userTokens[i]= null;
            	break;
            }    		
		}
    }
    
    private boolean isOnUserToken(String token) {
    	for (int i = 0; i < userTokens.length; i++) {
            if (userTokens[i] != null && userTokens[i].optString("token","").equals(token)) 
                return true;       	    
    	}
    	return false;
    }
    
    private String buscarTokenPeloIp(InetAddress ip) {
    	String token = "";
    	for (JSONObject userToken : userTokens) {
            if (userToken != null && (ip.toString()).equals(userToken.optString("ip", ""))) {
                token = userToken.optString("token", ""); 
                return token;
            }
        }
        return token;
    }
   
    private boolean isValidToken(String token) {
    	for (JSONObject userToken : userTokens) {
            if (userToken != null && token.equals(userToken.optString("token", ""))) {
                return true; // token e id válidos
            }
        }
        return false;     
    }
    
    private String hashearSenha(String senha) {
    	return BCrypt.hashpw(senha, BCrypt.gensalt());
    }

    private boolean isValidEmail(String email) {
        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    
    private boolean isEmailAlreadyRegistered(String email) {
        for (JSONObject cadastro : cadastros) {
            if (cadastro != null && email.equals(cadastro.optString("email", ""))) {
                return true; // E-mail já registrado
            }
        }
        return false; // E-mail não encontrado
    }
    
    public void imprimeTokens() {
    	for (int i = 0; i < userTokens.length; i++) {
            if (userTokens[i] != null ) {
            	System.out.println("Posicao: "+i +" "+userTokens[i].optString("usuarioId","")+": token:"+userTokens[i].optString("token",""));
            }    	
    	}
    }
    
    private class ServerListener implements Runnable {
        private int portNumber;

        public ServerListener(int portNumber) {
            this.portNumber = portNumber;
        }

        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            	InetAddress serverAddress = InetAddress.getLocalHost();
                log("Servidor aguardando conexões no Ip/Porta: " + serverAddress.getHostAddress() + "/" + portNumber + " ...");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    log("Servidor: Cliente conectado: " + clientSocket.getInetAddress()+ ":"+ clientSocket.getLocalPort()+"/");
                    new ClientHandler(clientSocket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        

        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    JSONObject mensagem = new JSONObject(inputLine);
                    log("Servidor <-Recebida do cliente " + clientSocket.getInetAddress()+":"+ mensagem);
                    String action = mensagem.optString("action", "");
                    JSONObject dataIn = mensagem.optJSONObject("data");
                    
                    switch (action) {
                        case "login":
                            if (dataIn != null) {
                                String email = dataIn.optString("email", "");
                                String senha = dataIn.optString("password", "");
                                
                                if (!isValidEmail(email)) {
                                    // Se o e-mail não for válido, responda ao cliente com um erro
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "login");
                                    resposta.put("error", true);
                                    resposta.put("message", "E-mail inválido.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }                                
                                if (validarLogin(email, senha)) {
                                	
                                	String token = JwtUtil.generateToken(String.valueOf(buscarId(email)), isAdmin(email));  
                                	addUserToken(token, clientSocket.getInetAddress());
                                	
                                	JSONObject resposta = new JSONObject();
                                	resposta.put("action","login");
                                	resposta.put("error","false");
                                	resposta.put("message","Login efetuado com sucesso!");
                                	JSONObject data = new JSONObject();
                                	data.put("token",token);
                                	resposta.put("data", data);
                                    log("Servidor->Enviada para o cliente "+ clientSocket.getInetAddress()+": " + resposta);
                                    out.println(resposta);

                                    break;
                                } else {
                                    JSONObject resposta = new JSONObject();
                                	resposta.put("action","login");
                                	resposta.put("error","true");
                                	resposta.put("message","Usuario ou Senha Invalidos!\n Tente Novamente");
                                	JSONObject data = new JSONObject();
                                	data.put("token","");
                                	resposta.put("data", data);
                                    out.println(resposta);
                                    log("Servidor->Enviada para o cliente "+ clientSocket.getInetAddress()+": " + resposta);
                                    break;
                                   
                                }
                            }
                            break;
                        
                        case "logout":
                            if (dataIn != null) {
                                String token = dataIn.optString("token", "");
                                if (isValidToken(token)) {    
                                    deleteUserToken(token);
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "logout");
                                    resposta.put("error", false);
                                    resposta.put("message", "Logout efetuado com sucesso.");
                                    log("Servidor->Enviada para o cliente "+ clientSocket.getInetAddress()+": " + resposta);
                                    out.println(resposta.toString());
                                } else {                                     
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "logout");
                                    resposta.put("error", true);
                                    resposta.put("message", "Token invalido. O logout falhou.");
                                    out.println(resposta.toString());
                                    log("Logout falhou no: "+ clientSocket.getInetAddress());
                                }
                                
                            }
                            break;

                            
                            
                        case "cadastro-usuario":
                            if (dataIn != null) {
                                String nome = dataIn.optString("name", "");
                                String token = dataIn.optString("token", "");
                                String email = dataIn.optString("email", "");
                                String tipo = dataIn.optString("type", "");
                                String senha = dataIn.optString("password", "");

                                // Validação de entrada
                                if (nome.isEmpty() || email.isEmpty() || tipo.isEmpty() || senha.isEmpty()) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "cadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falha ao cadastrar. Dados de entrada incompletos.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);

                                    break;
                                }
                                
                                if (!isValidEmail(email)) {
                                    // Se o e-mail não for válido, responda ao cliente com um erro
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "cadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "E-mail inválido.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                
                                if (isEmailAlreadyRegistered(email)) {
                                    // Se o e-mail já estiver cadastrado, responda ao cliente com um erro
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "cadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "E-mail ja cadastrado.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }

                                // Verifica permissões de usuário
                                if (!JwtUtil.isUserAdmin(token) && tipo.equals("admin")) {
                                    // Responde ao cliente com erro se o usuário não tem permissão para cadastrar um administrador
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "cadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falha ao cadastrar. Usuário sem permissão para cadastrar administradores.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);

                                } else {
                                    int usuarioId = encontrarPosicaoVaziaCadastros();

                                    if (usuarioId >= 0 && usuarioId < cadastros.length) {
                                        // Cria um novo cadastro de usuário
                                        JSONObject novoCadastro = new JSONObject();
                                        novoCadastro.put("name", nome);
                                        novoCadastro.put("email", email);
                                        novoCadastro.put("type", tipo);
                                        novoCadastro.put("password", hashearSenha(senha));
                                        cadastros[usuarioId] = novoCadastro;

                                        // Responde com sucesso ao cliente
                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "cadastro-usuario");
                                        resposta.put("error", false);
                                        resposta.put("message", "Usuário cadastrado	 com sucesso!");
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                        out.println(resposta.toString());

                                    } else {
                                        // Responde ao cliente com erro se o vetor não tem posição válida
                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "cadastro-usuario");
                                        resposta.put("error", true);
                                        resposta.put("message", "Falha ao cadastrar. Sem espaço para armazenar.");
                                        out.println(resposta.toString());
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);

                                    }
                                }
                            }
                            break;
                            
                        case "autocadastro-usuario":
                            if (dataIn != null) {
                                String nome = dataIn.optString("name", "");
                                String email = dataIn.optString("email", "");
                                String tipo = "user";
                                String senha = dataIn.optString("password", "");

                                // Validação de entrada
                                if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "autocadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falha ao cadastrar. Dados de entrada incompletos.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);

                                    break;
                                }
                                if (!isValidEmail(email)) {
                                    // Se o e-mail não for válido, responda ao cliente com um erro
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "autocadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "E-mail invalido.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                
                                if (isEmailAlreadyRegistered(email)) {
                                    // Se o e-mail já estiver cadastrado, responda ao cliente com um erro
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "autocadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "E-mail ja cadastrado.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }

                                    int usuarioId = encontrarPosicaoVaziaCadastros();

                                    if (usuarioId >= 0 && usuarioId < cadastros.length) {
                                        // Cria um novo cadastro de usuário
                                        JSONObject novoCadastro = new JSONObject();
                                        novoCadastro.put("name", nome);
                                        novoCadastro.put("email", email);
                                        novoCadastro.put("type", tipo);
                                        novoCadastro.put("password", hashearSenha(senha));
                                        cadastros[usuarioId] = novoCadastro;

                                        // Responde com sucesso ao cliente
                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "autocadastro-usuario");
                                        resposta.put("error", false);
                                        resposta.put("message", "Usuário cadastrado com sucesso!");
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                        out.println(resposta.toString());

                                    } else {
                                        // Responde ao cliente com erro se o vetor não tem posição válida
                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "autocadastro-usuario");
                                        resposta.put("error", true);
                                        resposta.put("message", "Falha ao cadastrar. Sem espaço para armazenar.");
                                        out.println(resposta.toString());
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);

                                    }
                                
                            }
                            break;


                            
                        case "listar-usuarios":
                            if (dataIn != null) {
                                String token = dataIn.optString("token", ""); // Obtém o token do usuário
                                JSONObject resposta = new JSONObject();
                                
                                if (token.isEmpty()) {
                                    resposta.put("action", "listar-usuarios");
                                    resposta.put("error", true);
                                    resposta.put("message", "Token vazio");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }
                                
                                if(!JwtUtil.isUserAdmin(token)) {
                                	
                                	resposta.put("action", "listar-usuarios");
                                    resposta.put("error", true);
                                    resposta.put("message", "Usuario sem permissao");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }
                                
                                JSONArray usuariosArray = new JSONArray();                                
                                for (int i = 0; i < cadastros.length; i++) {
                                    JSONObject usuario = cadastros[i];
                                    if (usuario != null) {
                                        JSONObject usuarioData = new JSONObject();
                                        usuarioData.put("id", i);
                                        usuarioData.put("name", usuario.optString("name", ""));
                                        usuarioData.put("type", usuario.optString("type", ""));
                                        usuarioData.put("email", usuario.optString("email", ""));
                                        usuariosArray.put(usuarioData);
                                    }
                                }

                                JSONObject dataResposta = new JSONObject();
                                resposta.put("action", "listar-usuarios");
                                resposta.put("error", false);
                                resposta.put("message", "Dados Listados com Sucesso!");
                                dataResposta.put("users", usuariosArray);
                                resposta.put("data", dataResposta);
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                out.println(resposta.toString());
                                }
                            
                            break;
                            
                        case "pedido-proprio-usuario":
                        	if (dataIn != null) {
                                String token = dataIn.optString("token", ""); // Obtém o token do usuário
                                JSONObject resposta = new JSONObject();
                                if (token.isEmpty()) {
                                    resposta.put("action", "pedido-proprio-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Token vazio");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }
                                resposta.put("action", "pedido-proprio-usuario");
                                resposta.put("error", false); 
                                resposta.put("message", "Sucesso");                                
                               	
                            	int id = Integer.parseInt(JwtUtil.getUserIdFromToken(token));
                            	JSONObject usuarioData = new JSONObject();
                                usuarioData.put("id", String.valueOf(id));
                                usuarioData.put("name", cadastros[id].optString("name", ""));
                                usuarioData.put("type", cadastros[id].optString("type", ""));
                                usuarioData.put("email", cadastros[id].optString("email", ""));
                                JSONObject dataResposta = new JSONObject();
                                resposta.put("action", "listar-usuarios");
                                resposta.put("error", false);
                                resposta.put("message", "Seus dados listados com sucesso! \nMotivo: Usuário: tipo 'user'!");
                                dataResposta.put("user", usuarioData);
                                resposta.put("data", dataResposta);
                                    
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                        	}
                        	break;

                            
                        case "pedido-edicao-usuario":
                            if (dataIn != null) {
                            	String token = dataIn.optString("token", "");
                                int usuarioId = dataIn.optInt("user_id", -1);
                                JSONObject resposta = new JSONObject();
                                resposta.put("action", "pedido-edicao-usuario");
                                if (usuarioId > cadastros.length || cadastros[usuarioId] == null) { 
                                	resposta.put("error", true);
                                    resposta.put("message", "Usuário não encontrado");
                                    resposta.put("data", "");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;                    
                                }
                                JSONObject cadastroSemSenha = new JSONObject();
                                cadastroSemSenha.put("name", cadastros[usuarioId].optString("name", ""));
                                cadastroSemSenha.put("email", cadastros[usuarioId].optString("email", ""));
                                cadastroSemSenha.put("type", cadastros[usuarioId].optString("type", ""));
                                
                                JSONObject usuarioData = new JSONObject(cadastroSemSenha.toString());
                                
                                if (!JwtUtil.isUserAdmin(token)) {
                                	resposta.put("error", true);
                                    resposta.put("message", "Usuario sem permissao");
                                    resposta.put("data", usuarioData);
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                } 

                                if (!(usuarioId >= 0 && usuarioId < cadastros.length && cadastros[usuarioId] != null)) { 
                                	resposta.put("error", true);
                                    resposta.put("message", "Usuário não encontrado");
                                    resposta.put("data", usuarioData);
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;                    
                                }
                                if (cadastroSemSenha.isEmpty()) {
                                	resposta.put("error", true);
                                    resposta.put("message", "Usuário encontrado encontrado porém sem informacoes no cadastro");
                                    resposta.put("data", usuarioData);
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;     
                                }
                                
                                resposta.put("error", false);
                                resposta.put("message", "Sucesso!");
                                resposta.put("data", usuarioData);
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                            }
                            break;
                            
                            
                            

						case "edicao-usuario":{
			                JSONObject resposta = new JSONObject();
							if (dataIn == null) {
					                // Envie uma resposta de erro ao cliente se "novos_dados" estiver ausente
					                resposta.put("action", "edicao-usuario");
					                resposta.put("error", true);
					                resposta.put("message", "Chave 'data' esta vazia");
					                out.println(resposta.toString());
					                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					                break;
					            }

							 String token = dataIn.optString("token", "");
							 String nome = dataIn.optString("name", "");
                             String email = dataIn.optString("email", "");
                             String tipo = dataIn.optString("type", "");
                             String senha = dataIn.optString("password", "");
                             int usuarioId = dataIn.optInt("user_id", -1);

						        if (usuarioId >= 0 && usuarioId < cadastros.length && cadastros[usuarioId] == null)  {
						            resposta.put("action", "edicao-usuario");
						            resposta.put("error", true);
						            resposta.put("message", "Usuário não encontrado.");
						            out.println(resposta.toString());
						            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					                break;
						        }
						        
						        if (!isValidEmail(email)){
						            resposta.put("action", "edicao-usuario");
						            resposta.put("error", true);
						            resposta.put("message", "Email invalido");
						            out.println(resposta.toString());
						            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					                break;
						        }
						        if ((!(cadastros[usuarioId].optString("email", "")).equals(email))&& isEmailAlreadyRegistered(email)) {						        	
									resposta.put("action", "edicao-usuario");
									resposta.put("error", true);
									resposta.put("message", "Email informado ja esta sendo utilizado em outro cadastro");
									out.println(resposta.toString());
									log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
									break;															        	
						        }
				                cadastros[usuarioId].put("name", nome);
				                cadastros[usuarioId].put("email", email);
				                cadastros[usuarioId].put("type", tipo);
				                if((!senha.isEmpty()) && (Integer.parseInt(JwtUtil.getUserIdFromToken(token)) == usuarioId)) {						                	
				                	cadastros[usuarioId].put("password", hashearSenha(senha));
				                } else if((!senha.isEmpty()) && (Integer.parseInt(JwtUtil.getUserIdFromToken(token)) != usuarioId)){
				                	resposta.put("action", "edicao-usuario");
						            resposta.put("error", true);
						            resposta.put("message", "Não autorizado! Você pode alterar somente sua própria senha.");
						            out.println(resposta.toString());
						            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					                break;
				                }
				                resposta.put("action", "edicao-usuario");
				                resposta.put("error", false);
				                resposta.put("message", "Alterações salvas com sucesso.");
				                out.println(resposta.toString());
				                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                if (isOnUserToken(token)) {
						        	deleteUserToken(token);
						        	addUserToken(token, clientSocket.getInetAddress());
						        }						        
				            } 
						    break;
						case "autoedicao-usuario":{
							
							if (dataIn == null) {
					                // Envie uma resposta de erro ao cliente se "novos_dados" estiver ausente
					                JSONObject resposta = new JSONObject();
					                resposta.put("action", "autoedicao-usuario");
					                resposta.put("error", true);
					                resposta.put("message", "Chave 'data' esta vazia");
					                out.println(resposta.toString());
					                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					                break;
					            }

							 String token = dataIn.optString("token", "");
							 String nome = dataIn.optString("name", "");
                             String email = dataIn.optString("email", "");
                             String tipo = dataIn.optString("type", "");
                             String senha = dataIn.optString("password", "");
                             int usuarioId = dataIn.optInt("id", -1);

						        if (usuarioId >= 0 && usuarioId < cadastros.length && cadastros[usuarioId] == null)  {
						            JSONObject resposta = new JSONObject();
						            resposta.put("action", "autoedicao-usuario");
						            resposta.put("error", true);
						            resposta.put("message", "Usuário não encontrado.");
						            out.println(resposta.toString());
						            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					                break;
						        }
						        if (Integer.parseInt(JwtUtil.getUserIdFromToken(token)) != usuarioId) {
					                // Envie uma resposta de erro ao cliente se "novos_dados" estiver ausente
					                JSONObject resposta = new JSONObject();
					                resposta.put("action", "autoedicao-usuario");
					                resposta.put("error", true);
					                resposta.put("message", "Você só pode alterar sua senha");
					                out.println(resposta.toString());
					                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					                break;
					            }

						        if (!isValidEmail(email)){
						            JSONObject resposta = new JSONObject();
						            resposta.put("action", "autoedicao-usuario");
						            resposta.put("error", true);
						            resposta.put("message", "Email invalido");
						            out.println(resposta.toString());
						            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					                break;
						        }
						        if ((!(cadastros[usuarioId].optString("email", "")).equals(email))&& isEmailAlreadyRegistered(email)) {						        	
									JSONObject resposta = new JSONObject();
									resposta.put("action", "autoedicao-usuario");
									resposta.put("error", true);
									resposta.put("message", "Email informado ja esta sendo utilizado em outro cadastro");
									out.println(resposta.toString());
									log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
									break;															        	
						        }
						        
						                // Atualize os dados do cadastro com as informações recebidas
						                cadastros[usuarioId].put("name", nome);
						                cadastros[usuarioId].put("email", email);
						                cadastros[usuarioId].put("type", tipo);
						                if(!senha.isEmpty()) {						                	
							                cadastros[usuarioId].put("password", hashearSenha(senha));
							                }
						                // Envie uma resposta de sucesso ao cliente
						                JSONObject resposta = new JSONObject();
						                resposta.put("action", "autoedicao-usuario");
						                resposta.put("error", false);
						                resposta.put("message", "Alterações salvas com sucesso.");
						                out.println(resposta.toString());
						                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
						                if (isOnUserToken(token)) {
								        	deleteUserToken(token);
								        	addUserToken(token, clientSocket.getInetAddress());
								        }
						            } 
						    break;
						    
						case "excluir-usuario":{
							if (dataIn == null) {
				                JSONObject resposta = new JSONObject();
				                resposta.put("action", "excluir-usuario");
				                resposta.put("error", true);
				                resposta.put("message", "Chave 'data' esta vazia");
				                out.println(resposta.toString());
				                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
							}

							 String token = dataIn.optString("token", "");
                             int usuarioId = dataIn.optInt("user_id", -1);
					         JSONObject resposta = new JSONObject();

					        if (usuarioId >= 0 && usuarioId < cadastros.length && cadastros[usuarioId] == null)  {
					            resposta.put("action", "excluir-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Usuário não encontrado.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }					
					        if (!JwtUtil.isUserAdmin(token)) {					        	
					            resposta.put("action", "excluir-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Usuário sem permissão !");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }  
			                resposta.put("action", "excluir-usuario");
			                resposta.put("error", false);
			                if (usuarioId == Integer.parseInt(JwtUtil.getUserIdFromToken(token)) ){
			                	resposta.put("message", "Excluiu seu próprio usuário com sucesso! Você sera desconectaco.\nFaça o login novamente");
					        	deleteUserToken(token);	
					        	out.println(resposta.toString());
				                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                cadastros[usuarioId]= null;
					        	out.close();
					        	break;
			                }else {
						    	resposta.put("message", "Usuário excluido com sucesso.");			                
				                out.println(resposta.toString());
				                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                cadastros[usuarioId]= null;
			                }
			            
						}
                    
						break;
						
						case "excluir-proprio-usuario":{
							if (dataIn == null) {
				                JSONObject resposta = new JSONObject();
				                resposta.put("action", "excluir-proprio-usuario");
				                resposta.put("error", true);
				                resposta.put("message", "Falha! Chave 'data' esta vazia");
				                out.println(resposta.toString());
				                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
							}

							 String token = dataIn.optString("token", "");
                             String email = dataIn.optString("email", "");
                             String senha = dataIn.optString("password", "");
                             int usuarioId = buscarId(email);
					         JSONObject resposta = new JSONObject();

					        if (usuarioId >= 0 && usuarioId < cadastros.length && cadastros[usuarioId] == null)  {
					            resposta.put("action", "excluir-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Falha! Usuário não encontrado.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }					
					        if (Integer.parseInt(JwtUtil.getUserIdFromToken(token)) != usuarioId) {					        	
					            resposta.put("action", "excluir-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Falha! Email diferente do cadastrado.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        } 
					        if (!validarLogin(email, senha)) {
					        	resposta.put("action", "excluir-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Falha! senha incorreta.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }
			                resposta.put("action", "excluir-usuario");
			                resposta.put("error", false);			                
		                	resposta.put("message", "Excluiu seu próprio usuário com sucesso! Você sera desconectaco.");
				        	deleteUserToken(token);	
				        	out.println(resposta.toString());
			                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
			                cadastros[usuarioId]= null;
				        	out.close();
				        	break;
			                }
                            
						default:
							
							break;
                }
                    } log("Servidor: Cliente desconectado: " + clientSocket.getInetAddress()+ ":"+ clientSocket.getLocalPort()+"/");
                    deleteUserToken(buscarTokenPeloIp(clientSocket.getInetAddress()));
                  
            } catch (IOException e) {
            	log("Servidor: Cliente desconectou no Ip:" + clientSocket.getInetAddress() + " mensagem: "+ e.getMessage());
            	deleteUserToken(buscarTokenPeloIp(clientSocket.getInetAddress()));
                
            }
            
        }


    }

    private void log(String message) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                logTextArea.append(formattedDateTime + " - " + message + "\n");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Servidor();
            }
        });
    }
}
