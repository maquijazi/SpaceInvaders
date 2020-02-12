/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codigo;

import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author jahaziel
 */
public class Marciano {

    public Image imagen1 = null;
    public Image imagen2 = null;
    public int posX = 0;
    public int posY = 0;
    private int anchoPantalla; //El marciano sabe cuanto mide la pantalla
    public int vida = 50;
    
    public void Mueve(boolean direccion){
        if (direccion){
            posX++;
        }
        else{
            posX--;
        }
    }

    public Marciano(int _anchoPantalla) {
        anchoPantalla = _anchoPantalla;
        try {
            imagen1 = ImageIO.read(getClass().getResource("/imagenes/marcianito1.png"));
            imagen2 = ImageIO.read(getClass().getResource("/imagenes/marcianito2.png"));
        } catch (IOException e) { //variable excepción, lo del catch no se  ejecuta
            System.out.println("ERORRRRRR");
        }

    }
}
