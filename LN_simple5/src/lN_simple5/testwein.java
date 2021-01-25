package lN_simple5;

import repast.simphony.random.RandomHelper;

public class testwein {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		double probability = 0.1;
		//double answer = calculateWeinbull(probability);
		for (int i=1; i<11; i++){
		double ans = RandomHelper.createNormal(11,2).nextInt();
		
		System.out.println("Wein bull = "+ ans);		
		
		calculateMaxRadius();
	
		}
		
	}
	
	public static double calculateWeinbull(double probability )
	{
		double P = probability;
		double K = 1.9113;
		double L=5.0219;
		double koverL= (K/L);
		double second=Math.pow((P/L),(K-1));
		double NewretentionPre=	Math.exp(-Math.pow((P/L),(K)));
		double answer= koverL* second *  NewretentionPre *3;
		System.out.println("retenteion" + koverL + " " + second + " "+ NewretentionPre + " "+answer);
		
		
		
		
		
		
	return answer;	
	}
	
	public static double calculateMaxRadius()
	{
		double OUTER_RADIUS = 33.0;
		double maxSwelling = 2.0;
		
		double maxvol = (4.0/3.0 * Math.PI * Math.pow(OUTER_RADIUS,3.0)) *maxSwelling;
		double maxRadius= Math.pow((3.0*maxvol/ (4.0*Math.PI)), (1.0/3.0));
		double gridsize = maxRadius+1;
		
		
		double answer = gridsize;
		
		System.out.println("Max Grid Radius needed = " + answer);
			
		
		
	return answer;	
	}
	
	
	
	
	
	
	
	

}
