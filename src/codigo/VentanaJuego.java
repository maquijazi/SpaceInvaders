/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codigo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Timer;

/**
 *
 * @author jorgecisneros
 */
public class VentanaJuego extends javax.swing.JFrame {

    static int ANCHOPANTALLA = 800; //static da la opción permite que las hijas puedan leer esta constante
    static int ALTOPANTALLA = 600;

    int filasMarcianos = 5;
    int columnasMarcianos = 10;
    int contador = 0;

    BufferedImage buffer = null;
    //buffer para guardar las imágenes de todos los marcianos
    BufferedImage plantilla = null;
    Image[] imagenes = new Image[30];

    //bucle de animacion del juego
    //en este caso, es un hilo de ejecucion nuevo que se encarga
    //de refrescar el contenido de la pantalla        
    Timer temporizador = new Timer(10, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //TODO: codigo de la animacion
            bucleDelJuego();
        }
    });

    Marciano miMarciano = new Marciano(ANCHOPANTALLA); //objeto de instancia
    Nave miNave = new Nave();
    Disparo miDisparo = new Disparo();

    //Comienzo de los array list
    ArrayList<Disparo> listaDisparos = new ArrayList();//Encierra el tipo de dato concreto. Realizado para multidisparo
    ArrayList<Explosion> listaExplosiones = new ArrayList();//Arraylist de explosiones

    Marciano[][] listaMarcianos = new Marciano[filasMarcianos][columnasMarcianos];//array de dos variables. Cajas sin marcianos aún.
    boolean direccionMarciano = true;

    /**
     * Creates new form VentanaJuego
     */
    public VentanaJuego() {//constructor
        initComponents();

        try {
            plantilla = ImageIO.read(getClass().getResource("/imagenes/invaders2.png"));
        } catch (IOException ex) {
        }
        //Cargo las 30 imagenes del spritesheet en el array de bufferedimages
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
                imagenes[i * 4 + j] = plantilla.getSubimage(j * 64, i * 64, 64, 64).getScaledInstance(32, 32, Image.SCALE_SMOOTH);

            }
        }

        imagenes[20] = plantilla.getSubimage(0, 320, 66, 32); //Cargo individualmente sprite nave
        imagenes[21] = plantilla.getSubimage(66, 320, 64, 32);
        imagenes[22] = plantilla.getSubimage(130, 320, 64, 32);//Explosion parteB
        imagenes[23] = plantilla.getSubimage(194, 320, 64, 32);//Explosion parteA

        setSize(ANCHOPANTALLA, ALTOPANTALLA);

        buffer = (BufferedImage) jPanel1.createImage(ANCHOPANTALLA, ALTOPANTALLA);
        buffer.createGraphics();

        //arranco el temporizador para que empiece el juego
        temporizador.start();

        miNave.imagen = imagenes[20];
        miNave.posX = ANCHOPANTALLA / 2 - miNave.imagen.getWidth(this) / 2;
        miNave.posY = ALTOPANTALLA - 100;
        for (int i = 0; i < filasMarcianos; i++) {
            for (int j = 0; j < columnasMarcianos; j++) {
                listaMarcianos[i][j] = new Marciano(ANCHOPANTALLA);
                listaMarcianos[i][j].imagen1 = imagenes[2 * i];
                listaMarcianos[i][j].imagen2 = imagenes[2 * i + 1];
                listaMarcianos[i][j].posX = j * (15 + listaMarcianos[i][j].imagen1.getWidth(null));
                listaMarcianos[i][j].posY = i * (10 + listaMarcianos[i][j].imagen1.getHeight(null));
            }

        }
        miDisparo.posY = -2000;
    }

    private void pintaMarcianos(Graphics2D _g2) {
        for (int i = 0; i < filasMarcianos; i++) {
            for (int j = 0; j < columnasMarcianos; j++) {
                listaMarcianos[i][j].Mueve(direccionMarciano);//Se arregla con una variable booleana
                if (listaMarcianos[i][j].posX == ANCHOPANTALLA - listaMarcianos[i][j].imagen1.getHeight(null) || listaMarcianos[i][j].posX <= 0) {
                    direccionMarciano = !direccionMarciano;
                    //Hago que todos los marcianos salten a la siguiente columna (la forma correcta debería estar en un método en marciano)
                    for (int k = 0; k < filasMarcianos; k++) {
                        for (int m = 0; m < columnasMarcianos; m++) {
                            listaMarcianos[k][m].posY += listaMarcianos[k][m].imagen1.getHeight(null);
                        }
                    }
                }
                if (contador < 50) {
                    _g2.drawImage(listaMarcianos[i][j].imagen1, listaMarcianos[i][j].posX, listaMarcianos[i][j].posY, null);
                } else if (contador < 100) {
                    _g2.drawImage(listaMarcianos[i][j].imagen2, listaMarcianos[i][j].posX, listaMarcianos[i][j].posY, null);
                } else {
                    contador = 0;
                }
            }
        }
    }

    private void pintaDisparos(Graphics2D g2) {//Metodo que pinta todos los disparos
        Disparo disparoAux;
        for (int i = 0; i < listaDisparos.size(); i++) {//listaDisparo.size() numero de elementos (disparos)
            disparoAux = listaDisparos.get(i);
            disparoAux.mueve();
            if (disparoAux.posY < 0) {
                listaDisparos.remove(i);//Elimino disparo que sale de la pantalla
            } else {
                g2.drawImage(disparoAux.imagen, disparoAux.posX, disparoAux.posY, null);//Aunque borras disparo, para pintarlo no tiene problema
            }

        }
    }

    private void bucleDelJuego() {//redibuja los objetos en el jPanel1
        //este metodo gobierna el redibujado de los objetos en el jpanel1

        //primero borro todo lo que hay en el buffer
        Graphics2D g2 = (Graphics2D) buffer.getGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, ANCHOPANTALLA, ALTOPANTALLA);
        contador++;
        pintaMarcianos(g2);

        ////////////////////////////////////////////////////
        /**
         * if (contador < 50) { g2.drawImage(miMarciano.imagen1, 10, 10,
         * null);//null: no aviso que se ha dibujado } else if (contador < 100)
         * { g2.drawImage(miMarciano.imagen2, 10, 10, null); } else { contador =
         * 0; }
         */
        //dibujo la nave
        g2.drawImage(miNave.imagen, miNave.posX, miNave.posY, null);
        //Dibujo imagen
        pintaDisparos(g2);//g2.drawImage(miDisparo.imagen, miDisparo.posX, miDisparo.posY, null);
        pintaExplosiones(g2);
        chequeaColision();

        //////////////////////////////////////////////////////
        //dibujo de golpe todo el buffer sobre el jpanel1
        g2 = (Graphics2D) jPanel1.getGraphics();
        g2.drawImage(buffer, 0, 0, null);
        miNave.mueve();
        //miDisparo.mueve();
        g2 = (Graphics2D) jPanel1.getGraphics();//dibujo de golpe el buffer sobre el jPanel
        g2.drawImage(buffer, 0, 0, null);
    }

    //chequea si un disparo y un marciano colisionan
    private void chequeaColision() {
        Rectangle2D.Double rectanguloMarciano = new Rectangle2D.Double();
        Rectangle2D.Double rectanguloDisparo = new Rectangle2D.Double();

        for (int k = 0; k < listaDisparos.size(); k++) {
            //calculo el rectangulo que contiene al disparo
            rectanguloDisparo.setFrame(listaDisparos.get(k).posX, listaDisparos.get(k).posY, listaDisparos.get(k).imagen.getWidth(null),
                    listaDisparos.get(k).imagen.getHeight(null));

            for (int i = 0; i < filasMarcianos; i++) {
                for (int j = 0; j < columnasMarcianos; j++) {
                    //calculo el rectángulo corresponmdiente al marciano que estoy comprobando
                    rectanguloMarciano.setFrame(listaMarcianos[i][j].posX, listaMarcianos[i][j].posY, listaMarcianos[i][j].imagen1.getWidth(null),
                            listaMarcianos[i][j].imagen1.getHeight(null));

                    if (rectanguloDisparo.intersects(rectanguloMarciano)) {
                        //Si entra aqui es porque han chocado un marciano y el disparo

                        Explosion e = new Explosion();
                        e.posX = listaMarcianos[i][j].posX;
                        e.posY = listaMarcianos[i][j].posY;
                        e.imagen1 = imagenes[23];
                        e.imagen2 = imagenes[22];
                        listaExplosiones.add(e);

                        listaMarcianos[i][j].posY = 2000;
                        listaDisparos.remove(k);
                    }
                }
            }
        }
    }
        
        

    private void pintaExplosiones(Graphics2D g2) {//Metodo que pinta todos las explosiones

        Explosion explosionAux;
        for (int i = 0; i < listaExplosiones.size(); i++) {
            explosionAux = listaExplosiones.get(i);
            explosionAux.tiempoDeVida--;
            if (explosionAux.tiempoDeVida > 25) {
                g2.drawImage(explosionAux.imagen1, explosionAux.posX, explosionAux.posY, null);
            }
            else{
               g2.drawImage(explosionAux.imagen2, explosionAux.posX, explosionAux.posY, null); 
            }
            //Si el tiempo de vida de la explosión es menos o igual que cero, la elimino
            if (explosionAux.tiempoDeVida >= 0){
                listaExplosiones.remove(i);
            }
        }

    }

    
        
        

    

    
        
        

    

    

    



    

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VentanaJuego.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VentanaJuego.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VentanaJuego.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VentanaJuego.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VentanaJuego().setVisible(true);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 525, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 404, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        // TODO add your handling code here:
        switch (evt.getKeyCode()) {//Casos de teclado

            case KeyEvent.VK_LEFT:
                miNave.setPulsadoIzquierda(true);
                break;
            case KeyEvent.VK_RIGHT:
                miNave.setPulsadoDerecha(true);
                break;
            case KeyEvent.VK_SPACE://Sugondo paso arrayList
                Disparo d = new Disparo();
                d.posicionaDisparo(miNave);
                //Agregamos disparo a listade disparos
                listaDisparos.add(d);
                break;

        }
    }//GEN-LAST:event_formKeyPressed

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        // TODO add your handling code here:
        switch (evt.getKeyCode()) {//Casos de teclado

            case KeyEvent.VK_LEFT:
                miNave.setPulsadoIzquierda(false);
                break;
            case KeyEvent.VK_RIGHT:
                miNave.setPulsadoDerecha(false);
                break;
        }
    }//GEN-LAST:event_formKeyReleased

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
