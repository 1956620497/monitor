package c.e;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;

@SpringBootTest
class MonitorServerApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }



    @Test
    void test(){
        //幻方
        int  n = 5;
//        if (n%2 == 0)
//            return;
        int[][] mat = new int[n][n];
        int i = 0,j = n/2;
        for (int k = 0; k < n*n; k++) {
            mat[i][j] = k;
            if (k % n == 0)
                i = (i+1)%n;
            else
            {
                i = (i-1+n)%n;
                j = (j+1)%n;
            }
        }
        //以下输出二维数组
        for (int k = 0; k < mat.length; k++) {
            for (int l = 0; l < mat[i].length; l++) {
                System.out.print(mat[k][l] + " ");
            }
            System.out.println();
        }
    }

    @Test
    void test2(){
        int n = 10;
        int mat[][] = new int[n][];
        int i,j;
        for (i = 0;  i<n ; i++) {
            mat[i] = new int[i+1];
            mat[i][0] = 1;
            mat[i][i] = 1;
            for (j=1;j<i;j++){
                mat[i][j] = mat[i-1][j-1]+mat[i-1][j];
            }
        }
        for (i = 0;  i< mat.length ; i++) {
            for (j = 0;  j<n-i ; j++)
                System.out.print(" ");

            for (j = 0;  j<mat[i].length ; j++)
                System.out.print(" "+mat[i][j]);
            System.out.println();
        }
    }
    @Test
    void test3(){
        print(create(4));
    }
    public static int[][] create(final int n){
        int[][] mat = new int[n][];
        for (int i = 0; i < n; i++) {
            mat[i] = new int[i+1];
            mat[i][0]=mat[i][i] = 1;
            for (int j = 1; j < i; j++) {
                mat[i][j] = mat[i-1][j-1]+mat[i-1][j];
            }
        }
        return mat;
    }
    public static void print(int[][] mat){
        for (int i = 0; i < mat.length; i++) {
            System.out.printf("%"+(mat.length-i+1)*2+"c",' ');
            for (int j = 0; j < mat[i].length; j++) {
                System.out.printf("%4d",mat[i][j]);
            }
            System.out.println();
        }
    }


}
