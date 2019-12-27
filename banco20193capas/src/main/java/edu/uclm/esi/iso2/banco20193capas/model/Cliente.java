package edu.uclm.esi.iso2.banco20193capas.model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

  @Entity
 
public class Cliente {

 @Id @GeneratedValue(strategy = GenerationType.AUTO)
 

 @Column(unique = true)

  private Long id;
  private String nif;
  private String nombre;
  private String apellidos;
  public Cliente() {
  }
  public Cliente(final String nif, final String nombre, final String apellidos) {
    this.nif = nif;
    this.nombre = nombre;
    this.apellidos = apellidos;
  }
  public final Long getId() {
    return id;
  }
  public void setId(final Long id) {
    this.id = id;
  }
  public String getNif() {
    return nif;
  }
  public void setNif(final String nif) {
    this.nif = nif;
  }
  public String getNombre() {
    return nombre;
  }
  public void setNombre(final String nombre) {
    this.nombre = nombre;
  }
  public String getApellidos() {
    return apellidos;
  }
  public void setApellidos(final String apellidos) {
    this.apellidos = apellidos;
  }
  public void insert() {
    Manager.getClienteDAO().save(this);
  }
}