/*
   Esta classe permite realizar o CHECKIN dos hóspedes no hotel

   No CHECKIN é informado apenas a data_entrada e adicional_veiculo,
   a data_saida e valor devem ser adicionadas no CHECKOUT

   O registro poderá ser criado com o nome, documento ou telefone do hóspede (via parâmetro)

   Para buscar todos os hóspedes que ainda estão no hotel, utilize GET http://localhost:8080/mavenproject5/webresources/checkin/
*/

package service;

import static java.lang.Integer.parseInt;
import java.text.ParseException;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import model.Checkin;

@Stateless
// Path URL para comandos REST na aplicação http://localhost:8080/HotelBack/webresources/checkin
@Path("checkin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CheckinService {

    @PersistenceContext(unitName = "Hotel-PU")
    private EntityManager em;

    public CheckinService() {
    }

    /*
       Retorna um JSON com todos os hóspedes que ainda estão no hotel
       GET http://localhost:8080/HotelBack/webresources/checkin/
    */
    @GET
    public List<Checkin> getCheckins() {
        String qlString = "SELECT c FROM Checkin c WHERE c.data_saida is null";
        Query query = em.createQuery(qlString);
        return query.getResultList();
    }

    /*
       Recebe algum dado do hóspede via parâmetro (nome, documento ou telefone) e
       busca no banco de dados o ID referente para fazer o CHECKIN no hotel.
       A data_saida deve ser informada somente no CHECKOUT e então o valor é calculado.
       POST http://localhost:8080/HotelBack/webresources/checkin/{hospede} (nome, documento ou telefone)
    
       Formato JSON:
       { "data_entrada": "20200101180000", "adicional_veiculo": true }
       Adote o formato yyyyMMddHHmmss para datas (a aplicação formata antes de salvar no banco de dados)
    */
    @POST
    @Path("{hospede}")
    public Checkin fazerCheckin(@PathParam("hospede") String hospede, Checkin checkin) {
        // Busca um hóspede com as informações recebidas por parâmetro (nome, documento ou telefone)
        Integer hospede_id = getHospedeId(hospede);
        // Caso retorne algum valor, verifica se o hóspede possui checkin em aberto (sem CHECKOUT - data de saída)
        if (hospede_id != null) {
            if (getCheckin(hospede) == null) {
                // Caso não possua um checkin em aberto, é criado um novo checkin
                checkin.setHospede_id(hospede_id);
                em.persist(checkin);
                return checkin;
            }
        }
        return null;
    }

    /*
       Recebe um dado do hóspede via parâmetro e um JSON com as informações para atualizar.
       A data_saida deve ser informada somente no CHECKOUT e então o valor é calculado.
    
       PUT http://localhost:8080/HotelBack/webresources/checkin/{hospede} (nome, documento ou telefone)
       Formato JSON:
       { "data_entrada": "20200101180000", "adicional_veiculo": true }
    */
    @PUT
    @Path("{hospede}")
    public Checkin atualizarCheckin(@PathParam("hospede") String hospede, Checkin checkinAtualizado) throws ParseException {
        // Verifica se existe algum checkin em aberto para o hóspede e então carrega as novas informações transmitidas no JSON (data_entrada e adicional_veiculo)
        try {
            Checkin checkinEncontrado = getCheckin(hospede);
            checkinEncontrado.setData_entrada(checkinAtualizado.getData_entrada());
            checkinEncontrado.setAdicional_veiculo(checkinAtualizado.getAdicional_veiculo());
            em.merge(checkinEncontrado);
            return checkinAtualizado;
        } catch (Exception e) {
            return null;
        }
    }

    /*
       Remove um CHECKIN da base com base no hóspede.
       Assim como o restante do REST, recebe o nome, documento ou telefone via parâmetro e tenta encontrar um hóspede no BD
       
       DELETE http://localhost:8080/HotelBack/webresources/checkin/{hospede}
    */
    @DELETE
    @Path("{hospede}")
    public Checkin excluirCheckin(@PathParam("hospede") String hospede) {
        try {
            Checkin checkin = getCheckin(hospede);
            em.remove(checkin);
            return checkin;
        } catch (Exception e) {
            return null;
        }

    }

    /*
       Retorna um JSON específico sobre um determinado hóspede (caso esteja com um CHECKIN em aberto)
    */
    @GET
    @Path("{hospede}")
    public Checkin getCheckin(@PathParam("hospede") String hospede) {
        Integer hospede_id = getHospedeId(hospede);
        if (hospede_id != null) {
            String sqlString = "SELECT id FROM Checkin WHERE hospede_id = " + hospede_id + " AND data_saida IS NULL";
            Query query = em.createNativeQuery(sqlString);
            try {
                // Como a criação dos CHECKINs é controlada, sempre irá existir no máximo um CHECKIN em aberto por hóspede
                // Por tanto é possível realizar uma query que busca por SingleResult.
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
}