package com.gestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.Optional;


///////////////////////////////////////////////////////////////////////
//****** MAIN ******
///////////////////////////////////////////////////////////////////////

public class App {
    public static void main( String[] args ) throws Exception {
      
      
      
      /////////////////////////////////////////////////////////////////
      // Main MVC
      /////////////////////////////////////////////////////////////////
        System.out.println( "EJEMPLO MVC" );
        
        ControladorLibros app = new ControladorLibros();
        app.ejecutar();

      /////////////////////////////////////////////////////////////////
      // Main Command Pattern 
      /////////////////////////////////////////////////////////////////
        System.out.println( "EJEMPLO COMMAND PATTERN" );

        Light light = new Light();
        RemoteControl remote = new RemoteControl();

        System.out.println("=== DEMOSTRACIÓN COMPLETA ===");
        System.out.println("Estado inicial: " + light);
        System.out.println();

        // Comandos individuales
        Command turnOn = new TurnOnCommand(light);
        Command turnOff = new TurnOffCommand(light);
        Command dim30 = new DimCommand(light, 30);
        Command dim80 = new DimCommand(light, 80);

        // Escenario 1: Encender y dimmer
        remote.setCommand(turnOn);
        remote.pressButton();  // ON
            
        remote.setCommand(dim30);
        remote.pressButton();  // 30%
            
        remote.setCommand(dim80);
        remote.pressButton();  // 80%
        
        remote.setCommand(turnOff);
        remote.pressButton();  // OFF

        System.out.println("\n--- Deshaciendo turnOff ---");
        remote.undo();  // Vuelve a 80%

        System.out.println("\n--- Deshaciendo dim 80 ---");
        remote.undo();  // Vuelve a 30%

        System.out.println("\n--- Deshaciendo dim 30 ---");
        remote.undo();  // Vuelve a ON

        System.out.println("\n--- Deshaciendo turnOn ---");
        remote.undo();  // Vuelve a OFF (estado inicial)

        System.out.println("\n--- Rehaciendo turnOn ---");
        remote.redo();  // ON

        System.out.println("\n=== MACRO COMMAND ===");
        MacroCommand macro = new MacroCommand();
        macro.addCommand(new TurnOnCommand(light));
        macro.addCommand(new DimCommand(light, 50));
        macro.addCommand(new DimCommand(light, 100));

        remote.setCommand(macro);
        remote.pressButton();  // ON -> 50% -> 100%

        System.out.println("\n--- Deshaciendo macro completa ---");
        remote.undo();  // Vuelve al estado antes de la macro

        System.out.println("\nEstado final: " + light);

      /////////////////////////////////////////////////////////////////
        System.out.println( "EJEMPLO TUI" );
    }    
}

///////////////////////////////////////////////////////////////////////
//****** PATRON ARQUITECTONICO MVC ******
///////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////
// Modelo
///////////////////////////////////////////////////////////////////////

class Libro {
    private int id;
    private String titulo;
    private String autor;
    private int año;
    private boolean prestado;

    public Libro(int id, String titulo, String autor, int año) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.año = año;
        this.prestado = false;
    }

    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public int getAño() { return año; }
    public boolean isPrestado() { return prestado; }

    // Setters
    public void setPrestado(boolean prestado) {
      this.prestado = prestado; 
    }

    @Override
    public String toString() {
        return String.format("[%d] %s - %s (%d) %s",
                id, titulo, autor, año,
                prestado ? "❌ Prestado" : "✅ Disponible");
    }
}


class ModeloLibros {
    private List<Libro> libros;
    private int nextId;

    public ModeloLibros() {
        this.libros = new ArrayList<>();
        this.nextId = 1;
        // Datos de ejemplo
        agregar("El Principito", "Saint-Exupéry", 1943);
        agregar("1984", "George Orwell", 1949);
        agregar("Cien años de soledad", "Gabriel García Márquez", 1967);
    }

    // CREATE
    public Libro agregar(String titulo, String autor, int año) {
        Libro libro = new Libro(nextId++, titulo, autor, año);
        libros.add(libro);
        return libro;
    }

    // READ (todos)
    public List<Libro> listar() {
        return new ArrayList<>(libros); // Copia defensiva
    }

    // READ (por ID)
    public Optional<Libro> buscar(int id) {
        for (Libro libro : libros) {
            if (libro.getId() == id) {
                return Optional.of(libro);
            }
        }
        return Optional.empty();
    }

    // UPDATE (prestar)
    public Optional<Libro> prestar(int id) {
        Optional<Libro> opt = buscar(id);
        if (opt.isPresent()) {
            Libro libro = opt.get();
            if (!libro.isPrestado()) {
                libro.setPrestado(true);
                return opt;
            }
        }
        return Optional.empty();
    }

    // UPDATE (devolver)
    public Optional<Libro> devolver(int id) {
        Optional<Libro> opt = buscar(id);
        if (opt.isPresent()) {
            Libro libro = opt.get();
            if (libro.isPrestado()) {
                libro.setPrestado(false);
                return opt;
            }
        }
        return Optional.empty();
    }

    // DELETE
    public Optional<Libro> eliminar(int id) {
        Optional<Libro> opt = buscar(id);
        if (opt.isPresent()) {
            libros.remove(opt.get());
            return opt;
        }
        return Optional.empty();
    }
}

///////////////////////////////////////////////////////////////////////
// Vista
///////////////////////////////////////////////////////////////////////

class VistaConsola {
    private Scanner scanner;

    public VistaConsola() {
        this.scanner = new Scanner(System.in);
    }

    public void mostrarMenu() {
        System.out.println("\n╔════════════════════════════╗");
        System.out.println("║   GESTOR DE LIBROS MVC     ║");
        System.out.println("╠════════════════════════════╣");
        System.out.println("║ 1. Agregar libro           ║");
        System.out.println("║ 2. Listar libros           ║");
        System.out.println("║ 3. Prestar libro           ║");
        System.out.println("║ 4. Devolver libro          ║");
        System.out.println("║ 5. Eliminar libro          ║");
        System.out.println("║ 6. Salir                   ║");
        System.out.println("╚════════════════════════════╝");
    }

    public String[] pedirDatosLibro() {
        System.out.print("Título: ");
        String titulo = scanner.nextLine().trim();
        System.out.print("Autor: ");
        String autor = scanner.nextLine().trim();
        System.out.print("Año: ");
        String año = scanner.nextLine().trim();
        return new String[]{titulo, autor, año};
    }

    public int pedirId() {
        System.out.print("ID del libro: ");
        while (!scanner.hasNextInt()) {
            System.out.print("❌ Ingresa un número válido: ");
            scanner.next();
        }
        int id = scanner.nextInt();
        scanner.nextLine(); // Limpiar buffer
        return id;
    }

    public void mostrarLibros(List<Libro> libros) {
        if (libros.isEmpty()) {
            System.out.println("📭 No hay libros registrados.");
            return;
        }
        System.out.println("\n📚 LISTA DE LIBROS:");
        System.out.println("────────────────────");
        for (Libro libro : libros) {
            System.out.println(libro);
        }
        System.out.println("────────────────────");
        System.out.println("Total: " + libros.size() + " libros");
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println("✅ " + mensaje);
    }

    public void mostrarError(String error) {
        System.out.println("❌ " + error);
    }

    public String leerOpcion() {
        System.out.print("👉 Elige una opción: ");
        return scanner.nextLine().trim();
    }
}

///////////////////////////////////////////////////////////////////////
// Controlador
///////////////////////////////////////////////////////////////////////

class ControladorLibros {
    private ModeloLibros modelo;
    private VistaConsola vista;

    public ControladorLibros() {
        this.modelo = new ModeloLibros();
        this.vista = new VistaConsola();
    }

    public void ejecutar() {
        boolean salir = false;

        while (!salir) {
            vista.mostrarMenu();
            String opcion = vista.leerOpcion();

            switch (opcion) {
                case "1":
                    agregarLibro();
                    break;
                case "2":
                    listarLibros();
                    break;
                case "3":
                    prestarLibro();
                    break;
                case "4":
                    devolverLibro();
                    break;
                case "5":
                    eliminarLibro();
                    break;
                case "6":
                    vista.mostrarMensaje("¡Hasta luego! 👋");
                    salir = true;
                    break;
                default:
                    vista.mostrarError(
                      "Opción inválida. " + 
                      "Intenta de nuevo."
                    );
            }
        }
    }

    private void agregarLibro() {
        String[] datos = vista.pedirDatosLibro();
        String titulo = datos[0];
        String autor = datos[1];
        String añoStr = datos[2];

        // Validaciones
        if (titulo.isEmpty() || autor.isEmpty()) {
            vista.mostrarError("Título y autor son obligatorios.");
            return;
        }

        int año;
        try {
            año = Integer.parseInt(añoStr);
            if (año < 0 || año > 2026) {
                vista.mostrarError("Año inválido (0-2026).");
                return;
            }
        } catch (NumberFormatException e) {
            vista.mostrarError("El año debe ser un número.");
            return;
        }

        Libro libro = modelo.agregar(titulo, autor, año);
        vista.mostrarMensaje(
          "Libro '" +
          libro.getTitulo() +
          "' agregado con ID " +
          libro.getId()
        );
    }

    private void listarLibros() {
        vista.mostrarLibros(modelo.listar());
    }

    private void prestarLibro() {
        int id = vista.pedirId();
        Optional<Libro> opt = modelo.prestar(id);

        if (opt.isPresent()) {
            Libro libro = opt.get();
            vista.mostrarMensaje(
              "Libro '" + 
              libro.getTitulo() + 
              "' prestado con éxito."
            );
        } else {
            vista.mostrarError(
              "No se puede prestar. " +
              "El libro no existe o ya está prestado."
            );
        }
    }

    private void devolverLibro() {
        int id = vista.pedirId();
        Optional<Libro> opt = modelo.devolver(id);

        if (opt.isPresent()) {
            Libro libro = opt.get();
            vista.mostrarMensaje(
              "Libro '" + 
              libro.getTitulo() + 
              "' devuelto con éxito."
            );
        } else {
            vista.mostrarError(
              "No se puede devolver. " + 
              "El libro no existe o no está prestado."
            );
        }
    }

    private void eliminarLibro() {
        int id = vista.pedirId();
        Optional<Libro> opt = modelo.eliminar(id);

        if (opt.isPresent()) {
            Libro libro = opt.get();
            vista.mostrarMensaje(
              "Libro '" + 
              libro.getTitulo() + 
              "' eliminado."
            );
        } else {
            vista.mostrarError(
              "No se encontró ningún libro con ID " + 
              id
            );
        }
    }
}



///////////////////////////////////////////////////////////////////////
//****** PATRON DE DISENO COMMAND ******
///////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////
// Interfaz Command
///////////////////////////////////////////////////////////////////////

interface Command {
    void execute();
    void undo();  // Añadimos undo a la interfaz
}

///////////////////////////////////////////////////////////////////////
// Receptor
///////////////////////////////////////////////////////////////////////

class Light {
    private boolean on = false;
    private int brightness = 50;

    // Getters para guardar estado
    public boolean isOn() { 
      return on; 
    }
    public int getBrightness() {
      return brightness; 
    }

    // Setters para restaurar estado
    public void setOn(boolean on) { 
      this.on = on; 
    }
    public void setBrightness(int brightness) { 
        this.brightness = Math.max(0, Math.min(100, brightness)); 
    }
  
    // Metodos
    public void turnOn() {
        // Logica
        on = true;
        System.out.println("🔆 Light is ON");
    }

    public void turnOff() {
        // Logica de negocio
        on = false;
        System.out.println("💡 Light is OFF");
    }

    public void dim(int level) {
        // Logica
        if (on) {
            brightness = Math.max(0, Math.min(100, level));
            System.out.println(
              "🎚️ Light dimmed to " + 
              brightness + 
              "%"
            );
        } else {
            System.out.println("⚠️ Cannot dim: Light is OFF");
        }
    }

    // Memento: guarda el estado actual
    public LightMemento saveState() {
        return new LightMemento(on, brightness);
    }

    // Memento: restaura un estado previo
    public void restoreState(LightMemento memento) {
        this.on = memento.on;
        this.brightness = memento.brightness;
        System.out.println("↩️ Estado restaurado: " + this);
    }

    @Override
    public String toString() {
        return "Light{" + 
          (on ? "ON" : "OFF") +
          ", brightness=" +
          brightness + 
          "%}";
    }

    // Clase Memento interna (inmutable)
    static class LightMemento {
        private final boolean on;
        private final int brightness;

        // Constructor
        private LightMemento(boolean on, int brightness) {
            this.on = on;
            this.brightness = brightness;
        }
        // Getters
        public boolean isOn() { 
          return on; 
        }
        public int getBrightness() { 
          return brightness; 
        }
    }
}

///////////////////////////////////////////////////////////////////////
// Comandos concretos
///////////////////////////////////////////////////////////////////////

class TurnOnCommand implements Command {
    private final Light light;
    private Light.LightMemento previousState;

    // Constructor
    public TurnOnCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        previousState = light.saveState();  // Guardamos estado previo
        light.turnOn();
    }

    @Override
    public void undo() {
        if (previousState != null) {
            light.restoreState(previousState);
        } else {
            System.out.println(
              "⚠️ No hay estado previo para restaurar"
            );
        }
    }
}

class TurnOffCommand implements Command {
    private final Light light;
    private Light.LightMemento previousState;

    // Constructor
    public TurnOffCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        previousState = light.saveState();
        light.turnOff();
    }

    @Override
    public void undo() {
        if (previousState != null) {
            light.restoreState(previousState);
        }
    }
}

class DimCommand implements Command {
    private final Light light;
    private final int level;
    private Light.LightMemento previousState;

    // Constructor
    public DimCommand(Light light, int level) {
        this.light = light;
        this.level = level;
    }

    @Override
    public void execute() {
        previousState = light.saveState();
        light.dim(level);
    }

    @Override
    public void undo() {
        if (previousState != null) {
            light.restoreState(previousState);
        }
    }
}


///////////////////////////////////////////////////////////////////////
// Comando Macro
///////////////////////////////////////////////////////////////////////

class MacroCommand implements Command {
    private final List<Command> commands = new ArrayList<>();
    private final List<Command> executedCommands = new ArrayList<>();

    public void addCommand(Command command) {
        commands.add(command);
    }

    @Override
    public void execute() {
        executedCommands.clear();
        for (Command cmd : commands) {
            cmd.execute();
            executedCommands.add(cmd);
        }
    }

    @Override
    public void undo() {
        // Deshace en orden inverso
        for (int i = executedCommands.size() - 1; i >= 0; i--) {
            executedCommands.get(i).undo();
        }
        executedCommands.clear();
    }
}

///////////////////////////////////////////////////////////////////////
// Invocador con Historial/Deshacer
///////////////////////////////////////////////////////////////////////

class RemoteControl {
    private final Stack<Command> history = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();
    private Command currentCommand;

    // Setters
    public void setCommand(Command command) {
        this.currentCommand = command;
    }

    public void pressButton() {
        if (currentCommand != null) {
            currentCommand.execute();
            history.push(currentCommand);
            redoStack.clear();  // Nuevo comando invalida el redo
            System.out.println(
              "✅ Comando ejecutado. Historial: " + 
              history.size()
            );
        }
    }

    public void undo() {
        if (!history.isEmpty()) {
            Command lastCommand = history.pop();
            lastCommand.undo();
            redoStack.push(lastCommand);
            System.out.println(
              "↩️ Undo ejecutado. Quedan: " + 
              history.size()
            );
        } else {
            System.out.println("❌ No hay comandos para deshacer");
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command commandToRedo = redoStack.pop();
            commandToRedo.execute();
            history.push(commandToRedo);
            System.out.println("↪️ Redo ejecutado");
        } else {
            System.out.println("❌ No hay comandos para rehacer");
        }
    }

    public void showHistory() {
        System.out.println(
          "📜 Historial: " + history.size() + 
          " comandos"
        );
    }
}
