package core;
import api.ClienteExchangeAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import modelos.HistorialConversion;
import modelos.LocalDateTimeAdapter;
import modelos.TasasDeCambio;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;


public class ConversorMonedas {
    private final ClienteExchangeAPI clienteAPI;
    private final List<HistorialConversion> historial = new ArrayList<>();
    private List<String> obtenerMonedasDisponibles(String monedaBase){
        try{
            TasasDeCambio tasas = clienteAPI.obtenerTasas(monedaBase).get();
            if (tasas != null && tasas.getRates() != null){
                return new ArrayList<>(tasas.getRates().keySet());
            }else {
                System.out.println("No  se pudieron obtener las tasas de cambio.");
            }
        }catch (InterruptedException | ExecutionException e){
            System.out.println("Error al obtener monedas" + e.getMessage());
        }
        return new ArrayList<>();
    }

    public ConversorMonedas(){
        this.clienteAPI = new ClienteExchangeAPI();
    }
    private double convertirMoneda(double cantidad, double tasaConversion){
        return  cantidad* tasaConversion;
    }
    //agrege metodos auxiliares de paginacion
    private void imprimirPagina(String[] monedas, int pagina, int porPagina, String tipo){
        int inicio = pagina * porPagina;
        int fin = Math.min(inicio + porPagina, monedas.length);
        int totalPaginas = (int) Math.ceil((double) monedas.length / porPagina);

        System.out.println("\nSeleccione la " + tipo + " ingresando el numero correspondiente: ");
        for (int i = inicio;  i< fin ; i++) {
            System.out.printf("%d. %s%n", (i - inicio +1), monedas[i]);
        }
        //Opciones adiccionales
        if (pagina > 0) System.out.println("P. PAGINA ANTERIOR");
        if (pagina < totalPaginas -1) System.out.println("N. PAGINA SIGUIENTE");
        System.out.println("0. Salir");
    }
    private int avanzarPagina (String[] lista, int paginaActual, int porPagina){
        int totalPaginas = (int)Math.ceil((double) lista.length / porPagina);
        return (paginaActual < totalPaginas - 1) ? paginaActual + 1 : paginaActual;
    }
    private int retrocederPagina(int paginaActual){
        return (paginaActual > 0) ? paginaActual - 1 : paginaActual;
    }
    private String obtenerInputUsuario(Scanner scanner){
        return  scanner.nextLine().trim().toUpperCase();
    }
    private String seleccionarMonedaDinamica(Scanner scanner, List<String> monedas, String tipo){
        int paginaActual = 0;
        int monedasPorPagina = 10;

        while (true){ //
            imprimirPagina(monedas.toArray(new String[0]), paginaActual, monedasPorPagina,tipo);
            String entrada = obtenerInputUsuario(scanner);

            switch (entrada){
                case "P":
                    paginaActual = retrocederPagina(paginaActual);
                    break;
                case "N":
                    paginaActual = avanzarPagina(monedas.toArray(new String[0]), paginaActual, monedasPorPagina);
                    break;
                case "0":
                    return null;
                default:
                    try {
                        int opcion = Integer.parseInt(entrada);
                        int inicio = paginaActual * monedasPorPagina;
                        int fin = Math.min(inicio + monedasPorPagina, monedas.size());

                        if (opcion >= 1 && opcion <= (fin - inicio)) {
                            return monedas.get(inicio + opcion - 1);
                        } else {
                            System.out.println("Opcion invalida. Intente nuevamente");
                        }
                    }catch (NumberFormatException e){
                        System.out.println("Entrada invalida. Intente nuevamente");
                    }

            }
        }
    }
    private void guardarHistorial(){
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        String ARCHIVO_HISTORIAL = "historial_conversiones.json";
        try (FileWriter writer = new FileWriter(ARCHIVO_HISTORIAL)){
            gson.toJson(historial, writer);
        }catch (IOException e){
            System.err.println("Error al guardar el historial: " + e.getMessage());
        }
    }

    public void iniciar() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean continuar = true;

            while (continuar) {
                System.out.println("**********************************");
                System.out.println("Bienvenido/a al Conversor de Monedas");

                List<String> monedasBase = List.of("USD","EUR","CLP", "ARS", "BOB", "BRL", "MXN", "CAD", "PEN", "COP",
                        "UYU", "PYG", "VEF", "GTQ", "HNL", "NIO", "CRC", "DOP", "JMD", "TTD");
                String monedaBase = seleccionarMonedaDinamica(scanner, monedasBase, "moneda base");
                if (monedaBase == null) break;

                if (monedaBase.equalsIgnoreCase("salir")) {
                    System.out.println("Gracias por usar el conversor. Hasta luego");
                    break;
                }
                List<String> monedasDisponibles = obtenerMonedasDisponibles(monedaBase);
                if (monedasDisponibles.isEmpty()) return;
                String monedaObjetivo = seleccionarMonedaDinamica(scanner, monedasDisponibles, "moneda objetivo");
                if (monedaObjetivo == null) return;


                System.out.println("Ingrese cantidad a convertir: ");
                while (!scanner.hasNextDouble()) {
                    System.out.println("Por favor ingrese un numero valido para la cantidad: ");
                    scanner.next();
                }
                double cantidad = scanner.nextDouble();
                scanner.nextLine();// limpiar buffer

                try {
                    TasasDeCambio tasas = clienteAPI.obtenerTasas(monedaBase).get();

                    if (tasas == null || tasas.getRates() == null) {
                        System.out.println("no se pudieron obtener las tasa de cambio");
                        return;
                    }

                    Double tasaConversion = tasas.getRates().get(monedaObjetivo);

                    if (tasaConversion == null) {
                        System.out.println("No se encontro tasa de cambio para " + monedaObjetivo);
                        return;
                    }

                    double resultado = convertirMoneda(cantidad, tasaConversion);
                    System.out.printf("%.2f %s = %.2f %s%n", cantidad, monedaBase, resultado, monedaObjetivo);

                    //Guardar en historial
                    historial.add(new HistorialConversion(monedaBase,monedaObjetivo, cantidad, resultado));
                    guardarHistorial();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Operacion interrupida");
                } catch (ExecutionException e) {
                    System.out.println("Error al obtener tasa de cambio: " + e.getCause().getMessage());
                }
                System.out.println("\n¿Desea hacer otra conversión? (s/n): ");
                String respuesta = scanner.nextLine().trim().toLowerCase();
                if (!respuesta.equals("s")) {
                    continuar = false;
                    System.out.println("Gracias por usar el conversor. Hasta luego!");
                }

            }
        }
    }

}