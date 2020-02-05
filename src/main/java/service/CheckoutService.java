/* 
   Esta classe é responsável pelo CHECKOUT do hóspede no hotel.
   Aqui é informado a data_saida e calculado o valor da estadia.
   
   Regras de negócio:
   - Uma diária no hotel de segunda à sexta custa R$120,00;
   - Uma diária no hotel em finais de semana custa R$150,00;
   - Caso a pessoa precise de uma vaga na garagem do hotel há um acréscimo diário, sendo R$15,00 de segunda à sexta e R$20,00 nos finais de semana;
   
   Para consultar os hóspedes que não estão mais no hotel, utilize GET http://localhost:8080/mavenproject5/webresources/checkout/
*/

package service;

import static java.lang.Integer.parseInt;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import model.Checkin;

@Stateless
// Path URL para comandos REST na aplicação http://localhost:8080/HotelBack/webresources/checkout
@Path("checkout")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CheckoutService {

    @PersistenceContext(unitName = "Hotel-PU")
    private EntityManager em;
    
    // Regras de negócio
    Integer diariaDiaUtil = 120;
    Integer veiculoDiaUtil = 15;        
    Integer diariaFds = 150;
    Integer veiculoFds = 20;
    Double valorFinal = 0.0;
    
    DayOfWeek dow;    
    List<LocalDate> allDates;
    
    public CheckoutService() {
        this.allDates = new ArrayList<>();
    }

    /*
       Retorna um JSON com todos os hóspedes que não estão mais no hotel
       GET http://localhost:8080/HotelBack/webresources/checkout
    */
    @GET
    public List<Checkin> getCheckouts() {
        Query query = em.createQuery("SELECT c FROM Checkin c WHERE c.data_saida is not null");
        return query.getResultList();
    }

    /*
       Recebe algum dado do hóspede (nome, documento ou telefone) para fazer o CHECKOUT do hotel
       A aplicação encontra o ID deste hóspede e verifica se existe algum CHECKIN em aberto (sem data de saída)
       Então, atualiza a data_saida deste registro para a data informada via JSON
       POST http://localhost:8080/HotelBack/webresources/checkout/{hospede) (nome, documento ou telefone)
    
       Formato JSON:
       { "data_saida": "20200101180000", "valor": 0.0 }
       Adote o formato yyyyMMddHHmmss para datas (a aplicação formata antes de salvar no banco de dados)
    */
    @POST
    @Path("{hospede}")
    public Checkin fazerCheckout(@PathParam("hospede") String hospede, Checkin checkout) throws ParseException {
        // Busca o ID do hóspede
        Integer hospede_id = getHospedeId(hospede);
        if (hospede_id != null) {
            // Verifica se existe algum checkin em aberto para o hóspede
            String sqlString = "SELECT id FROM Checkin WHERE hospede_id = " + hospede_id + " AND data_saida is null";
            Query query = em.createNativeQuery(sqlString);
            try {
                Checkin checkoutEncontrado = em.find(Checkin.class, query.getSingleResult());
                
                /*
                   Reseta a váriavel do valor da diária e chama a função responsável por
                   popular a array allDates (que contém todas as datas entre a data_entrada e data_saida)
                   para calcular individualmente o valor de cada dia em dias úteis e finais de semana
                */
                valorFinal = 0.0;
                getDatesBetween(checkoutEncontrado.getData_entrada(), checkout.getData_saida());
                calcularDiaria(checkoutEncontrado.getAdicional_veiculo());
                
                // Define a data_saida como a data recebida no JSON e o valor calculado
                checkoutEncontrado.setData_saida(checkout.getData_saida());
                checkoutEncontrado.setValor(valorFinal);
                em.merge(checkoutEncontrado);
                return checkoutEncontrado;                
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /*
       Retorna informações especificas sobre o CHECKOUT de um determinado hóspede (via parâmetro na URL)
       GET http://localhost:8080/HotelBack/webresources/checkout/{hospede) (nome, documento ou telefone)
    */
    @GET
    @Path("{hospede}")
    public Checkin getCheckout(@PathParam("hospede") String hospede) {
        // Busca o ID do hóspede com base nas informações do parâmetro
        Integer hospede_id = getHospedeId(hospede);
        if (hospede_id != null) {
            // Seleciona um CHECKIN sem data_saida (em aberto)
            String sqlString = "SELECT id FROM Checkin WHERE hospede_id = " + hospede_id + " AND data_saida is not null";
            Query query = em.createNativeQuery(sqlString);
            try {
                return em.find(Checkin.class, query.getSingleResult());
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    //Método responsável por encontrar um hóspede com base no seu nome, documento ou telefone, normalmente passados via parâmetro no REST
    public Integer getHospedeId(String hospede) {
        String sqlInfo
                = "SELECT id FROM Hospede WHERE documento = '" + hospede + "' OR telefone = '" + hospede + "' OR nome = '" + hospede + "'";
        Query query = em.createNativeQuery(sqlInfo);
        try {
            return parseInt(query.getSingleResult().toString());
        } catch (Exception e) {
            return null;
        }
    }
    
        public final void getDatesBetween(String startDate, String endDate) {
        
        String[] splitStart = startDate.split(" ");
        String[] splitEnd = endDate.split(" ");
        
        LocalDate incrementingDate = LocalDate.parse(splitStart[0]);
        LocalDate endDt = LocalDate.parse(splitEnd[0]);

        if (incrementingDate.isAfter(endDt)) {
            throw new IllegalStateException("A data de entrada deve ser anterior ou igual a data de saída.");
        }

        while (!incrementingDate.isAfter(endDt)) {
            allDates.add(incrementingDate);
            incrementingDate = incrementingDate.plusDays(1);
        }
    }
    
    /* Método responsável por calcular o valor da diária
       Antes de executá-lo, atualize a listagem das datas entre data_entrada e data_saida
       com POST http://localhost:8080/HotelBack/webresources/checkout/{hospede) (nome, documento ou telefone)
       + JSON { "data_saida": "20200101180000", "valor": 0.0 }
        
      As datas são informadas diretamente em getDatesBetween(). Este método recebe apenas o Boolean adicional_veiculo.
    */  
    public final void calcularDiaria(Boolean adicionalVeiculo) {
        // Cria um laço for para percorrer todas as datas entre data_entrada e data_saida
        for (int i = 0; i < allDates.size(); i++) {
            // Para cada registro, obtém o DayOfWeek e converte para String para verificar qual dia da semana é e calcular o valor da diária correspondente
            dow = allDates.get(i).getDayOfWeek();
            String day = dow.toString();
            // Caso seja dia útil
            if (day == "MONDAY" || day == "TUESDAY" || day == "WEDNESDAY" || day == "THURSDAY" || day == "FRIDAY") {
                valorFinal = valorFinal + diariaDiaUtil;
                if (adicionalVeiculo == true) {
                    valorFinal = valorFinal + veiculoDiaUtil;
                }
            }
            // Caso seja final de semana
            if (day == "SATURDAY" || day == "SUNDAY") {
                valorFinal = valorFinal + diariaFds;
                if (adicionalVeiculo == true) {
                    valorFinal = valorFinal + veiculoFds;
                }
            }
        }
    }

}
