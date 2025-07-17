package core;
import api.ClienteExchangeAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import modelos.HistorialConversion;
import modelos.TasasDeCambio;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class ConversorMonedas {
    private final ClienteExchangeAPI clienteAPI;
    private final String[] monedasValidas ={ "ARS", "BOB", "BRL", "CLP", "USD"};
    private final List<HistorialConversion> historial = new ArrayList<>();

    public ConversorMonedas(){
        this.clienteAPI = new ClienteExchangeAPI();
    }
    private double convertirMoneda(double cantidad, double tasaConversion){
        return  cantidad* tasaConversion;
    }
    private String seleccionarMoneda(Scanner scanner, String tipo){
        while (true){
            System.out.println("\nSeleccione la " + tipo + " ingresando el numero correspondiente: ");
            for (int i = 0;  i< monedasValidas.length ; i++) {
                System.out.printf("%d. %s%n", i + 1, monedasValidas[i]);
            }
            System.out.println("0. Salir");

            while (!scanner.hasNextInt()){
                System.out.println("Por favor ingrese un numero valido:");
                scanner.next();
            }

            int opcion = scanner.nextInt();
            scanner.nextLine(); //Limpiar buffer

            if (opcion == 0) return null;
            if (opcion >= 1 && opcion <= monedasValidas.length){
                return  monedasValidas[opcion - 1];
            }else {
                System.out.println("Opcion invalida. Intente nuevamente");
            }
        }
    }
    private void guardarHistorial(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
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

                String monedaBase = seleccionarMoneda(scanner, "moneda base");
                if (monedaBase == null) break;

                if (monedaBase.equalsIgnoreCase("salir")) {
                    System.out.println("Gracias por usar el conversor. Hasta luego");
                    break;
                }
                String monedaObjetivo = seleccionarMoneda(scanner, "moneda objetivo");
                if (monedaObjetivo == null) break;


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