/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cifsha1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Personal
 */
public class CifSHA1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String cad = "El hijo de rana, Rinrín renacuajo salió esta mañana muy tieso" 
                + ", muy majo. Con pantalón corto, corbata a la moda, sombrero encintado"
                +" y chupa de boda.";
        System.out.println("Mensaje: "+ cad);
        int[] cadArrI = new int[cad.length()];
        
        //Se convierte la cadena a arreglo de codigos ascii 
        for (int i = 0; i < cadArrI.length; i++) {
            cadArrI[i] =  cad.charAt(i);
        }
        
        String[] cadArrBin = new String[cadArrI.length];
        
        //Convertir ascii a numeros binarios de 1 byte(8bits)
        for (int i = 0; i < cadArrI.length; i++) {
            cadArrBin[i] = intToBin(cadArrI[i]);
        }
        
        //Juntar todos los binarios y agregar un 1 al final 
        String cadBinWith1 =  joinCadWith1(cadArrBin);
        /*Agregar Ceros al final hasta que la longitud de la cadena sea un numero 
        que al ser sumado con (512-448) sea un multiplo de 512*/
        String cadBin512Mod448 = addTill512mod448(cadBinWith1);
        /*Tomar la longitud de la cadena y convertir ese numero a binario de 64 bits*/
        int lenCadBin = cadBinWith1.length() -1 ; 
        String lenBinCadBin = intToBin(lenCadBin);
        lenBinCadBin = addTill64(lenBinCadBin);
        /*Juntar la cadena binaria de longitud 512*n + 448 con la longitud de 64 bits
        para generar una cadena binaria multiplo de 512*/
        String cadBinMult512 = cadBin512Mod448;
        cadBinMult512 += lenBinCadBin;
        
        //Se rompe la cadena binaria en partes iguales de 512 bits
        String[] chunksOf512Bin = splitCadIntoChunksOf512(cadBinMult512);
        
        //Se genera un arreglo de 16 partes iguales de 32 bits para cada chunk 
        String[][] arrChunks16Words = splitChunksInto16Words(chunksOf512Bin);

        /*Se genera un arreglo de 32 partes donde el xor entre 2 partes 
        separadas 16 posiciones da como resultado una de las 16 partes de
        32 bits originales para cada chunk*/
        String[][] arrChunks32Words = calcXORArr16Words(arrChunks16Words);
        
        //Se establece una llave de 32 bits(4 caracteres)
        System.out.println("Se cifra con una llave de 4 caracteres");
        String llave = "HOLW";
        System.out.println("Llave : " + llave);
        
        if (llave.length() != 4) {
            System.out.println("La llave debe tener 4 caracteres");
        }
        else{
            int num; 
            String llaveBin=""; 
            //Se convierte la llave a numero binario de 32 bits
            for (int i = 0; i < llave.length(); i++) {
                
                num =  llave.charAt(i);
                llaveBin += intToBin(num);
            }
            
            /*Se aplica XOR entre la llave y el arreglo de 32 palabras para 
            cada chunk */          
            String[][] cifArrChunks32Words = cifArr(arrChunks32Words, llaveBin);
            
            //Se juntan todas las palabras de 32 bits para la salida           
            String outputBin = joinCifArrChunks32Words(cifArrChunks32Words);
            
            
            //Se genera una salida hexadecimal por cada palabra de 32 bits
            String outputHex = binToHex(outputBin);
            
            System.out.println("Mensaje cifrado: " + outputHex);
            
            //Se transfoma la salida a binario por cada hexadecimal de 8 bits
            String outputHexToBin = hexToBin(outputHex);
            
            /*Se busca volver a generar el mismo arreglo de arreglos de binarios 
          de 32 bits */
            String[][] ArrFromOutput = genArrFromOutput(outputHexToBin);
            
            /*Se aplica XOR entre la llave y el arreglo cifrado de 32 palabras para 
            cada chunk para descifrar los numeros binarios*/
            String[][] decifArrChunks32Words = decifArr(ArrFromOutput, llaveBin);

            /*Se obtiene el arreglo de chunks cada uno con las 16 palabras 
            originales aplicando  xor entre todas las parejas de cadenas 
            separadas 16 posiciones */
            String[][] decifArrChunks16Words = decifCalcXORArr32Words(decifArrChunks32Words);
            
            /*Se obtiene el arreglo de chunks de 512 bits juntando las 16 palabras
            para cada chunk*/
            
            String[] decifChunksOf512Bin = joinArrChunks16Words(decifArrChunks16Words);
            
            /*Se juntan todos los chunks de 512 bits*/
            String decifCadBinMult512 = joinChunksOf512Bin(decifChunksOf512Bin);
            
            /*Se debe eliminar los 64 ultimos caracteres que no hacen parte del mensaje*/    
            String decifCadBin512Mod448 = decifCadBinMult512.substring(0, decifCadBinMult512.length() - 64);
            
            /*Se deben eliminar todos los ceros al final de la cadena hasta encontrar
            el 1 que fue añadido manualmente en el proceso inicial de cifrado*/
            String decifCadBin = remZerosTillOne(decifCadBin512Mod448);
            /*Se traducen los grupos de 8 bits a caracteres*/
            String decifCad = decifCadBinToCad(decifCadBin);
            
            System.out.println("Mensaje descifrado: " +  decifCad);
        }
    }
    
    public static String binToHex(String binCad32){
        int num;
        String hexCad="",temp;
        for (int i = 0; i < binCad32.length(); i+= 32) {
            num = Integer.parseInt(binCad32.substring(i, i + 32),2);
            temp = Integer.toString(num, 16);
            while (temp.length() < 8) {                
                temp = "0" + temp;
            }
            hexCad += temp;
        }
        return  hexCad;
    }
    public static String hexToBin(String hexCad32){
        BigInteger num;
        String binCad="",temp;
        for (int i = 0; i < hexCad32.length(); i+= 8) {
            num = new BigInteger(hexCad32.substring(i, i + 8),16);
            temp = num.toString(2);
            while (temp.length() < 32) {                
                temp = "0" + temp;
            }
            binCad += temp;
        }
        return  binCad;
    }
    public static String decifCadBinToCad( String decifCadBin){
        String decifCad = "";
        Integer val = 0;
        int valInt = 0;
        for (int i = 0; i < decifCadBin.length(); i = i + 8) {
            if (i + 8 >= decifCadBin.length()) {
                val = Integer.parseInt(decifCadBin.substring(i, decifCadBin.length()),2);
            }
            else{
                val = Integer.parseInt(decifCadBin.substring(i, i + 8),2);
            }
            valInt = val.intValue();
            decifCad += (char)valInt;
        }
        return decifCad;
    }
    public static String remZerosTillOne( String decifCadBin512Mod448){
        
        String decifCadBin = "";
        
        for (int i = decifCadBin512Mod448.length() - 1; i > 0; i--) {
            if (decifCadBin512Mod448.charAt(i) == '1') {
                decifCadBin = decifCadBin512Mod448.substring(0, i);
                break;
                
            }
        }
        return  decifCadBin;
    }
    
    public static String joinChunksOf512Bin(String[]  decifChunksOf512Bin){
        String decifCadBinMult512 = "";
        
        for (int i = 0; i < decifChunksOf512Bin.length; i++) {
            decifCadBinMult512 += decifChunksOf512Bin[i];
        }
        return decifCadBinMult512;
    }
    public static String[] joinArrChunks16Words (String[][] decifArrChunks16Words){
        
        String[] decifChunksOf512Bin = new String[decifArrChunks16Words.length];
        
        for (int i = 0; i < decifChunksOf512Bin.length; i++) {
            decifChunksOf512Bin[i] = "";
            for (int j = 0; j < 16; j++) {
                decifChunksOf512Bin[i] += decifArrChunks16Words[i][j];
            }
        }
        
        return decifChunksOf512Bin;
    }
    public static String[][] genArrFromOutput (String output){
//        Cada chunk tiene 1024 espacios porque se generaron parejas para cada 
//        cadena binaria de 32 bits, y siendo 16 palabras por chunk, serian 32
//        palabras por chunk y 32x32 = 1024
        String[] ArrFromOutput = new String[output.length()/1024];
        String temp;
        int posArr =0;
        for (int i = 0; i < output.length(); i +=1024) {
            temp = output.substring(i, i+1024);
            ArrFromOutput[posArr] = temp;
            posArr++;
        }
        
        String[][] arrChunks32Words = splitChunksInto32Words(ArrFromOutput);
        return arrChunks32Words;
    }
    
    public static String[][] splitChunksInto32Words(String[] chunksOf1024Bin){
        String[][] arrChunks32Words = new String[chunksOf1024Bin.length][32];
        String chunkOf1024Bin;
        String temp;
        int posArr;
        
        for (int i = 0; i < chunksOf1024Bin.length; i++) {
            chunkOf1024Bin = chunksOf1024Bin[i];
            posArr = 0;
            for (int j = 0; j < 1024; j += 32) {
                temp = chunkOf1024Bin.substring(j, j + 32);
                arrChunks32Words[i][posArr] = new String(temp);
                posArr++;
            }
        }
//        for (int i = 0; i < arrChunks16Words[0].length; i++) {
//            System.out.println(arrChunks16Words[0][i]);
//        }
        return arrChunks32Words;
    }
    
    public static String joinCifArrChunks32Words(String[][] cifArrChunks32Words){
        String output = "";
        
        for (int i = 0; i < cifArrChunks32Words.length; i++) {
            for (int j = 0; j < cifArrChunks32Words[i].length; j++) {
                output += cifArrChunks32Words[i][j];
            }
        }
        return output;
    }
    
    public static String[][] cifArr (String[][] arrChunks32Words, String llaveBin){
        String[][] cifArrChunks32Words = new String[arrChunks32Words.length][32];
        
        String[] chunk32Words;
        for (int i = 0; i < arrChunks32Words.length; i++) {
            chunk32Words = arrChunks32Words[i];
            for (int j = 0; j < chunk32Words.length; j++) {
                cifArrChunks32Words[i][j] = calcXORtwoBin32(chunk32Words[j], llaveBin);
            }
        }
        
        return cifArrChunks32Words;
    
    }
    public static String[][] decifArr (String[][] cifArrChunks32Words, String llaveBin){
        String[][] decifArrChunks32Words = new String[cifArrChunks32Words.length][32];
        
        String[] chunk32Words;
        for (int i = 0; i < cifArrChunks32Words.length; i++) {
            chunk32Words = cifArrChunks32Words[i];
            for (int j = 0; j < chunk32Words.length; j++) {
                decifArrChunks32Words[i][j] = calcXORtwoBin32(chunk32Words[j], llaveBin);
            }
        }
        
        return decifArrChunks32Words;
    
    }
    
    public static String[][] decifCalcXORArr32Words (String[][] arrChunks32Words){
        
        String[][] decifArrChunks16Words = new String[arrChunks32Words.length][16]; 
        String[] chunk32Words;
        int posRes = 0;
        
        for (int i = 0; i < arrChunks32Words.length; i++) {
            chunk32Words = arrChunks32Words[i];
            posRes = 0; 
            
            for (int j = 0; j < 16; j++) {
                decifArrChunks16Words[i][j] = calcXORtwoBin32(chunk32Words[posRes], chunk32Words[posRes + 16]);
                posRes++;
            }
        }
        
        return decifArrChunks16Words;
    }
    
    public static String[][] calcXORArr16Words(String[][] arrChunks16Words){
        List<String> chunk16Words;
        String a,b,c,d, valXOR;
        String[][] arrChunks32XorRes  = new String[arrChunks16Words.length][32]; 
        int posRes; 
        for (int i = 0; i < arrChunks16Words.length; i++) {
            chunk16Words = new ArrayList<>();
            Collections.addAll(chunk16Words, arrChunks16Words[i]);
            posRes = 0; 
            for (int j = 16; j < 32; j++) {
                a = chunk16Words.get(j-3);
                b = chunk16Words.get(j-8);
                c = chunk16Words.get(j-14);
                d = chunk16Words.get(j-16);
                valXOR = calcXORtwoBin32(a, b);
                valXOR = calcXORtwoBin32(valXOR, c);
                valXOR = calcXORtwoBin32(valXOR, d);
                
                arrChunks32XorRes[i][posRes] = valXOR;
                arrChunks32XorRes[i][posRes + 16] = calcXORtwoBin32(valXOR, chunk16Words.get(posRes));
                chunk16Words.add(valXOR);
                posRes ++;
            }
        }
        return arrChunks32XorRes;
    }
    public static String calcXORtwoBin32(String a,String b ){
        
        String c = "";
        
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) == b.charAt(i)) {
                c += 0;
            }
            else{
                c += 1;
            }
        }
        
        return c;
    }
    
    public static String[][] splitChunksInto16Words(String[] chunksOf512Bin){
        String[][] arrChunks16Words = new String[chunksOf512Bin.length][16];
        String chunkOf512Bin;
        String temp;
        int posArr;
        
        for (int i = 0; i < chunksOf512Bin.length; i++) {
            chunkOf512Bin = chunksOf512Bin[i];
            posArr = 0;
            for (int j = 0; j < 512; j += 32) {
                temp = chunkOf512Bin.substring(j, j + 32);
                arrChunks16Words[i][posArr] = new String(temp);
                posArr++;
            }
        }
//        for (int i = 0; i < arrChunks16Words[0].length; i++) {
//            System.out.println(arrChunks16Words[0][i]);
//        }
        return arrChunks16Words;
    }
    
    public static String[] splitCadIntoChunksOf512(String cadBinMult512){
        String[] arrChunks512 = new String[cadBinMult512.length()/512];
        String temp;
        int posArr =0;
        for (int i = 0; i < cadBinMult512.length(); i +=512) {
            temp = cadBinMult512.substring(i, i+512);
            arrChunks512[posArr] = temp;
            posArr++;
        }
        return arrChunks512;
    }
    
    public static String addTill64(String lenCadBin){
        String newLen = "";
        int numZeros = 64 - lenCadBin.length();
        while (newLen.length() < numZeros) {            
            newLen += 0;
        }
        newLen += lenCadBin;
        return newLen;
    }
    
    public static String addTill512mod448(String cadBinWith1){
        
        String cadBin512Mod448 = new String(cadBinWith1);
    
        while (cadBin512Mod448.length() % 512 != 448 ) {            
            cadBin512Mod448 += 0;
        }
        return  cadBin512Mod448; 
    }
    
    public static String joinCadWith1(String[] cadArrBin){
        
        String cadBinWith1 = "";
        
        for (int i = 0; i < cadArrBin.length; i++) {
            cadBinWith1 += cadArrBin[i];
        }
        cadBinWith1 += 1;
        return cadBinWith1;
    }
    
    public static String intToBin(int num){
        String binNum = new String();
       
        while (num > 1){
            int residuo = num % 2; 
            binNum += residuo;
            num /= 2;
            if (num <= 1) {
                binNum += num;
            }
        }
        StringBuilder binRev = new StringBuilder(binNum);
        binRev.reverse();
        binNum = binRev.toString();

        int excesoCeros = binNum.length() - 8;
        if (excesoCeros > 0) {
            for (int i = 0; i < excesoCeros; i++) {
                binNum = binNum.substring(1, binNum.length());
            }
        }
        if (excesoCeros < 0) {
            excesoCeros *= -1;
            for (int i = 0; i < excesoCeros; i++) {
                binNum = 0 + binNum;
            }
        }
        return binNum;
    }
}
