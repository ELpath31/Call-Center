import javax.swing.*;
import java.awt.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


class Llamada {
    private String cliente;

    public Llamada(String cliente) {
        this.cliente = cliente;
    }

    public String getCliente() {
        return cliente;
    }

    @Override
    public String toString() {
        return cliente;
    }
}

class Agente implements Runnable {
    private String nombre;
    private BlockingQueue<Llamada> cola;
    private JLabel labelEstado;

    public Agente(String nombre, BlockingQueue<Llamada> cola, JLabel labelEstado) {
        this.nombre = nombre;
        this.cola = cola;
        this.labelEstado = labelEstado;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Llamada llamada = cola.take();
                SwingUtilities.invokeLater(() -> labelEstado.setText(nombre + " atendiendo a " + llamada.getCliente()));
                Thread.sleep(1000);
                SwingUtilities.invokeLater(() -> labelEstado.setText(nombre + " Libre"));
            }
        } catch (InterruptedException e) {
            SwingUtilities.invokeLater(() -> labelEstado.setText(nombre + " finalizó su turno"));
        }
    }
}

public class CallCenterGUIControl {
    public static void main(String[] args) {
        BlockingQueue<Llamada> cola = new LinkedBlockingQueue<>();
        DefaultListModel<Llamada> modeloLista = new DefaultListModel<>();

        JFrame frame = new JFrame("Call Center con Control de Clientes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400);
        frame.setLayout(new BorderLayout());


        
        JPanel panelAgentes = new JPanel(new GridLayout(2, 1, 10, 10));
        JLabel agente1Label = new JLabel("Agente 1 Libre"); //Hilos
        JLabel agente2Label = new JLabel("Agente 2 Libre"); //Hilos
        panelAgentes.setBorder(BorderFactory.createTitledBorder("Agentes"));
        panelAgentes.add(agente1Label);
        panelAgentes.add(agente2Label);


        JList<Llamada> listaClientes = new JList<>(modeloLista);
        JScrollPane scrollClientes = new JScrollPane(listaClientes);
        scrollClientes.setBorder(BorderFactory.createTitledBorder("Clientes en Cola"));


        
        JPanel panelBotones = new JPanel(new FlowLayout());

        JButton generarClienteBtn = new JButton("Generar Cliente");
        JComboBox<Llamada> comboClientes = new JComboBox<>();
        JButton quitarClienteBtn = new JButton("Quitar Cliente");


        generarClienteBtn.addActionListener(e -> {
            int clienteNum = modeloLista.getSize() + 1;
            Llamada llamada = new Llamada("Cliente " + clienteNum);
            try {
                cola.put(llamada);
                modeloLista.addElement(llamada);
                comboClientes.addItem(llamada);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });


        quitarClienteBtn.addActionListener(e -> {
            Llamada seleccion = (Llamada) comboClientes.getSelectedItem();
            if (seleccion != null) {
                cola.remove(seleccion);
                modeloLista.removeElement(seleccion); 
                comboClientes.removeItem(seleccion); 
            }
        });

        panelBotones.add(generarClienteBtn);
        panelBotones.add(comboClientes);
        panelBotones.add(quitarClienteBtn);

        frame.add(panelAgentes, BorderLayout.NORTH);
        frame.add(scrollClientes, BorderLayout.CENTER);
        frame.add(panelBotones, BorderLayout.SOUTH);

        frame.setVisible(true);


        new Thread(() -> {
            while (true) {
                if (!cola.isEmpty() && modeloLista.contains(cola.peek())) {
                    modeloLista.removeElement(cola.peek());
                    comboClientes.removeItem(cola.peek());
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();


        new Thread(new Agente("Agente 1", cola, agente1Label)).start();
        new Thread(new Agente("Agente 2", cola, agente2Label)).start(); //hilos
    }
}


