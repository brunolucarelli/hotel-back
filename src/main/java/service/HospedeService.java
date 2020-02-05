/* Essa classe é responsável pelo CRUDL dos hóspedes com REST + PostgreSQL + Eclipselink (EntityManager)
*/

package service;

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
import model.Hospede;

@Stateless
// Path URL para comandos REST na aplicação http://localhost:8080/HotelBack/webresources/hospedes
@Path("hospedes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HospedeService {
    
    @PersistenceContext(unitName = "Hotel-PU")
    private EntityManager em;
    
    public HospedeService() {
    }

    /* 
       Retorna um JSON com todos os hóspedes cadastros no banco de dados.
       GET http://localhost:8080/HotelBack/webresources/hospedes
    */
    @GET
    public List<Hospede> getHospedes() {
        String qlString = "SELECT h FROM Hospede h";
        Query query = em.createQuery(qlString);
        return query.getResultList();
    }
    
    /*
       Persiste um novo hóspede no banco de dados.
       POST http://localhost:8080/HotelBack/webresources/hospedes
       
       Formato JSON:
       { "nome": "Fulano", "documento": "123", "telefone": "123" }
    */
    @POST
    public Hospede adicionar(Hospede hospede) {
        em.persist(hospede);
        return hospede;
    }
    
    /*
       Atualiza um hóspede cadastrado no banco de dados.
       O ID do hóspede deve ser passado via parâmetro no final da URL.
       PUT http://localhost:8080/HotelBack/webresources/hospedes/{id}
       
       Formato JSON:
       { "nome": "Fulano", "documento": "123", "telefone": "123" }
    */
    @PUT
    @Path("{id}")
    public Hospede atualizar(@PathParam("id") Integer id, Hospede hospedeAtualizado) {
        Hospede hospedeEncontrado = getHospede(id);
        hospedeEncontrado.setNome(hospedeAtualizado.getNome());
        hospedeEncontrado.setDocumento(hospedeAtualizado.getDocumento());
        hospedeEncontrado.setTelefone(hospedeAtualizado.getTelefone());
        em.merge(hospedeEncontrado);
        return hospedeAtualizado;
    }
    
    /*
       Exclui um hóspede da base.
       O ID do hóspede deve ser passado via parâmetro no final da URL.
       DELETE http://localhost:8080/HotelBack/webresources/hospedes/{id}
    */
    @DELETE
    @Path("{id}")
    public Hospede excluir(@PathParam("id") Integer id) {
        Hospede hospede = getHospede(id);
        em.remove(hospede);
        return hospede;
    }
    
    /*
       Retorna um JSON com as informações de um hóspede específico.
       O ID deste hóspede deve ser passado por parâmetro no final da URL.
       GET http://localhost:8080/HotelBack/webresources/hospedes/{id}
    */
    @GET
    @Path("{id}")
    public Hospede getHospede(@PathParam("id") Integer id) {
        return em.find(Hospede.class, id);
    }
}