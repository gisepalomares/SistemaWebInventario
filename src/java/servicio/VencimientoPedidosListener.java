/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicio;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Cada minuto vence los pedidos "pagar en tienda" que superaron su plazo (24 a 48 h)
 * y libera el stock que tenían reservado. Arranca con la aplicación y se detiene con ella.
 */
@WebListener
public class VencimientoPedidosListener implements ServletContextListener {

    private ScheduledExecutorService planificador;

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        final ServletContext contexto = evento.getServletContext();
        final PedidoFacade facade = new PedidoFacade();

        planificador = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "vencimiento-pedidos");
                t.setDaemon(true);
                return t;
            }
        });

        planificador.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                // Se captura todo: si la tarea lanza una excepción, el planificador dejaría de ejecutarla
                try {
                    int vencidos = facade.vencerPedidos();
                    if (vencidos > 0) {
                        contexto.log("Pedidos vencidos y con stock liberado: " + vencidos);
                    }
                } catch (Exception e) {
                    contexto.log("No se pudo revisar el vencimiento de pedidos", e);
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        if (planificador != null) {
            planificador.shutdownNow();
        }
    }
}
