package entrega1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DadosUsuario {
    private String nome;
    private String email;
    private String senha;
    private String tipo;
    private String token;
    private String username;

    @JsonCreator
    public DadosUsuario(
            @JsonProperty("nome") String nome,
            @JsonProperty("email") String email,
            @JsonProperty("senha") String senha,
            @JsonProperty("tipo") String tipo,
            @JsonProperty("token") String token,
            @JsonProperty("username") String username) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
        this.token = token;
        this.setUsername(username);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
