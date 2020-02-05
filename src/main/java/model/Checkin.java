package model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "checkin")
public class Checkin implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "data_entrada")
    private String data_entrada;
    
    @Column(name = "data_saida")
    private String data_saida;
    
    @Column(name = "adicional_veiculo")
    private Boolean adicional_veiculo;
    
    @Column(name = "hospede_id")
    private Integer hospede_id;
    
    @Column(name = "valor")
    private Double valor;
    
    public Checkin() {
    }

    public Checkin(Integer id, String data_entrada, String data_saida, Boolean adicional_veiculo, Integer hospede_id, Double valor) {
        this.id = id;
        this.data_entrada = data_entrada;
        this.data_saida = data_saida;
        this.adicional_veiculo = adicional_veiculo;
        this.hospede_id = hospede_id;
        this.valor = valor;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getData_entrada() {
        return data_entrada;
    }

    public void setData_entrada(String data_entrada) throws ParseException {
        Date dtEntrada = new SimpleDateFormat("yyyyMMddHHmmss").parse(data_entrada);
        String data_entradaFormatada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dtEntrada);
        this.data_entrada = data_entradaFormatada;
    }

    public String getData_saida() {
        return data_saida;
    }

    public void setData_saida(String data_saida) throws ParseException {
        Date dtSaida = new SimpleDateFormat("yyyyMMddHHmmss").parse(data_saida);
        String data_saidaFormatada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dtSaida);
        this.data_saida = data_saidaFormatada;
    }
    
    public Boolean getAdicional_veiculo() {
        return adicional_veiculo;
    }

    public void setAdicional_veiculo(Boolean adicional_veiculo) {
        this.adicional_veiculo = adicional_veiculo;
    }

    public Integer getHospede_id() {
        return hospede_id;
    }

    public void setHospede_id(Integer hospede_id) {
        this.hospede_id = hospede_id;
    }
    
    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
    
}
