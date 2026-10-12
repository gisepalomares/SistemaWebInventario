/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidad;

/**
 *
 * @author USER
 */
public class Proveedor {

    private int idProveedor;
    private String ruc;
    private String razonSocial;
    private String nombreComercial;

    public Proveedor() {
    }

    /** Nombre que se muestra en pantallas: el comercial si existe, si no la razón social. */
    public String getNombreMostrar() {
        return (nombreComercial != null && !nombreComercial.trim().isEmpty()) ? nombreComercial : razonSocial;
    }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }
}