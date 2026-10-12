/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import dao.CategoriaDAO;
import interfaces.ICategoriaDAO;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Página de inicio de la tienda en línea.
 * URL: /inicio
 */
@WebServlet(name = "InicioServlet", urlPatterns = {"/inicio"})
public class InicioServlet extends HttpServlet {

    private final ICategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            request.setAttribute("categorias", categoriaDAO.listar());
        } catch (SQLException e) {
            // La portada igual se muestra; la sección de categorías avisa que no está disponible
            log("Error al cargar las categorías de la portada", e);
        }

        request.getRequestDispatcher("/WEB-INF/vistas/publico/inicio.jsp").forward(request, response);
    }
}