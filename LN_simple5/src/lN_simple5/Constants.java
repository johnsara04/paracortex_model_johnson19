package lN_simple5;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

public class Constants {
	
   
	//import param file first (constants that are varied in sensitivity analysis)
	static Parameters params = RunEnvironment.getInstance().getParameters();
	
	//COUNTERS AND SWITCHES,0=off, 1=on
	public static final int TrackingSwitch = 0;
	public static final int timeTostartTracking= 13000;
	public static final int CellCountON = 1;
	public static final int RecruitmentSwitch = 1;
	public static final int numTcellsToTrack=100;
	
	//VALUES FOR MODEL SET UP - PRE-EQUILIBRIUM
	public static final int TICK_BV_RECRUITMENT_CHANGE=220;// time at which entry/exit area percentage may change
	public static final int TICK_VOLUME_CHANGE=80;
	public static final int TICK_AREAS_RADIUS_CHANGE = 250;
	public static final int TICK_DCS_ENTER = 700;//time stimuli applied 
	
	//SETTING UP THE GEOMETRY 
	public static final int SIMULATION_LENGTH =50000;//( each unit represents 20seconds)
	public static final double OUTER_RADIUS =33;//paracortex radius, each unit represents 6μm
	public static final double INNER_RADIUS= OUTER_RADIUS-2;//inner radius is to 2 units less than outer
	
	// EXPANSION INITIAL PARAMETERS (sigmoidal function parameterized in params)
	//**** Overall grid size is set up after max swelling, to ensure 
    // world size is larger than the paracortex max volume *****//
	public static final double maxSwelling =(Double)params.getValue("maxSwelling");
	//options:1=no expansion, 1.5 = 50% more  2= 2x etc. and 55 = maintain 55% T cell occupation 
	
	static double maxvol = (4.0/3.0 * Math.PI * Math.pow(OUTER_RADIUS,3.0)) *maxSwelling;
	static double maxRadius= Math.pow((3.0*maxvol/ (4.0*Math.PI)), (1.0/3.0));
	static double gridsize_temp = maxRadius+1;
	public static final int GRID_SIZE = (int) gridsize_temp*2;//convert to integer values
	public static final int GRID_WIDTH =(int) gridsize_temp; // it's a half node therefore need half the size in 1 dimension
	public static final int CENTRE_POINT = GRID_SIZE/2;
	public static final double ENTRY_PERCENTAGE = 0.5;//percent of the radius that defines entry area
	public static final double EXIT_PERCENTAGE = 0.93;	
	public static final double SCS_height = 0.7;// define SCS cap
	
	// AGENT SET UP (fixed T cell and DC parameters)
	public static final double Occupation=0.55; //fraction of total volume occupied by T cells
	public static final double Initial_Tcellcount1= Math.round((0.5*Constants.Occupation*4/3*3.14*(Math.pow(  (((int)INNER_RADIUS)*6),3)))/150);
	public static final int Initial_Tcellcount=(int) Initial_Tcellcount1;
	
	public static int DC_reach_radius= 2; //units = 6μm (equiv to DC size)
    public static final double RatioCD4 = 0.70; 
    public static final double RatioCD8 = 0.30; 
    public static final double fract_aff = 0.1; //proportion of tcells that enter in afferent lymph
    public static final double fract_HEV = 0.9; // proportion of tcells that enter in HEVs
       
    //agent set up : lifespans
    public static final int DClifespan=10800; //sd =500 2-3 days (1-9days) and since peaks at acton 2 day
 	public static final int lifespanT= 216000;//365 days /sd = 5000 naive life span
	public static final int lifespan_ActT=7380;//41hours sd20
	public static final int  lifespan_EffT = 15120;// 3.5days
    
    
    //VARIABLE PARAMETERS - pulled in from param file
 
    //movement and transit 
	public static final double Pe= (Double)params.getValue("Pe"); //probability of egress
	public static final double Beta = (Double)params.getValue("beta");//probability of movement 
	public static final double Crowding_rules =(int)params.getValue("Crowding_rules"); //1-3= max 1-3, 4 = least crowded method
	public static final int Tres =4320;//T cell residence time 
    
    
    //T cell response thresholds

	public static final int ACTIVATION_MEAN_CD4=(int)params.getValue("ACTIVATION_MEAN_CD4");//1000;
	public static final double ACTIVATION_CURVE_CD4=ACTIVATION_MEAN_CD4/(-(Math.log ((1/0.15)-1)));
	public static final int ACTIVATION_MEAN_CD8=(int)params.getValue("ACTIVATION_MEAN_CD8");//1400;
	public static final double ACTIVATION_CURVE_CD8=ACTIVATION_MEAN_CD8/(-(Math.log ((1/0.15)-1)));
	    
	public static final int EFFECTOR_MEAN_CD4=(int)params.getValue("EFFECTOR_MEAN_CD4");//90;//
	public static final double EFFECTOR_CURVE_CD4=EFFECTOR_MEAN_CD4/(-(Math.log ((1/0.03)-1))); 
	public static final int EFFECTOR_MEAN_CD8=(int)params.getValue("EFFECTOR_MEAN_CD8");//30;//
	public static final double EFFECTOR_CURVE_CD8=EFFECTOR_MEAN_CD8/(-(Math.log ((1/0.05)-1)));

	    
	public static final int proftimeCD4=(int)params.getValue("proftimeCD4"); //(1980, 180))  //11hours,1hour
	public static final int proftimeCD8=(int)params.getValue("proftimeCD8"); //1260,180 7hours +/- 1hour
	public static final int MaxProfCD8=(int)params.getValue("MaxProfCD8"); //16 max proliferations
	public static final int MaxProfCD4=(int)params.getValue("MaxProfCD4");//10 max proliferations
	//ratios for the memory cell differentiation 
	public static final double early_dif_ratio =(Double)params.getValue("early_dif_ratio");//0.01;
	public static final double late_dif_ratio =(Double)params.getValue("late_dif_ratio");//0.01;0.04;
	    
   
	//interaction dynamics
	public static final double noncog_interaction=(Double)params.getValue("noncog_interaction");
	public static final int short_bind=(int)params.getValue("short_bind"); //10-15min (+5min)
	public static final int long_bind=(int)params.getValue("long_bind"); //50-70min  (+20min)
	//point where T cells switch from short to long interactions
	public static final int time_bind_change=(int)params.getValue("time_bind_change");// 8hours 
	public static final int MAX_BIND_PER_STEP= (int)params.getValue("MAX_BIND_PER_STEP");    //Max number of T cells a single DC can bind in one step
	public static final int DC_MAX_BOUND = (int)params.getValue("DC_MAX_BOUND");//15; //Max number of T cells a single DC can bind
	    
		
	//stimulation parameters
	public static final double Ks = (Double)params.getValue("Ks");//stim gain factor 0.016 
	public static final double STIM_DECAY =(Double)params.getValue("decay");// 0.999;//8;public static final int ACTIVATION_MEAN_A=1000;
	public static final int initialDC= 0; // if an initial number of DCs is desired (primed LN resident) 
	public double TOTAL_DC_COUNT = 0;// CAN DELETE if switch to use iterator only
    public static final double MHCstartvalue=(Double)params.getValue("MHCstartvalue");
	public static final double halflife1=(Double)params.getValue("halflife1");//MHCI half life 19.7h LAMBDA_1=0.0001976467; 
	public static final double halflife2=(Double)params.getValue("halflife2"); //MHCII hl, 60h LAMBDA_2=0.0000641802945;     
	public final static double cognition =(Double)params.getValue("cognition");//should be 0.0001 with 10^5
	
	// DC entry parameters
	public static final double DCpercentage = (Double)params.getValue("DCpercentage");//4% = ~2500
    public static final int DC_ADDITION_TIME =(int)params.getValue("DC_ADDITION_TIME");//Duration of stimuli entering 10800;//(2.5days) 
	public static final int DC_ADDITION_INTERVAL=40;
    
	//S1P1 parameters : relative expression
	public final static double S1P1postEntry =(Double)params.getValue("S1P1postEntry");
	public final static double S1P1act =(Double)params.getValue("S1P1act");
	public final static double S1P1eff_late =(Double)params.getValue("S1P1eff_late");
	public final static double S1P1eff_early =(Double)params.getValue("S1P1eff_early");
	public final static double S1P1mem =(Double)params.getValue("S1P1mem");
    public final static double S1P1all_inflam =(Double)params.getValue("S1P1all_inflam");
    
    //time T cells require post entry or inflammation detection to re-upregulate S1P1r
    public static final double timePostEntryS1P1up=(int)params.getValue("timePostEntryS1P1up");//90 ticks = 1h
    public static final int responsetimeInflam=(int)params.getValue("responsetimeInflam");//720=4h	
   
    
    // Recruitment: thresholds for increased inflammation
    public static final double recruitmentT1=(Double)params.getValue("recruitmentT1");  
    public static final double recruitmentT2 = (Double)params.getValue("recruitmentT2");
    public static final double recruitmentFactor =(Double)params.getValue("recruitmentFactor");
     

    //LN ENLARGEMENT PARAMETERS >
    //Max Volume is set earlier to determine overall grid size 
    public static final double midpoint =(Double)params.getValue("midpoint");
    public static final double slope =(Double)params.getValue("slope");


   
    // FILE PATHS FOR DATA SINKS - mostly used to test mobility so can be ignored in general
   
    // Random number used to label results in batch runs
    public static int randomnumber = RandomHelper.nextIntFromTo(1000,100000);
    //File names
    public static final String DESTINATION="/$WORK/scj13/velocity_cm/v5volume2";
    public static final String FILE_NAME0 = (System.getProperty("user.dir"))+ "/track_"+ randomnumber+ "track";
    public static final String FILE_NAME1 =  (System.getProperty("user.dir"))+ "/Transit_All_"+ randomnumber+ ".csv";
    public static final String FILE_NAME2 =  (System.getProperty("user.dir"))+ "/Transit_cog_"+ randomnumber+ ".csv";
    public static final String FILE_NAME3 =  (System.getProperty("user.dir"))+ "/Transit_act_"+ randomnumber+ ".csv";
    public static final String FILE_NAME4 =  (System.getProperty("user.dir"))+ "/Transit_eff_"+ randomnumber+ ".csv";
    public static final String FILE_NAME5 =  (System.getProperty("user.dir"))+"/Transit_mem_"+ randomnumber+ ".csv";
    public static final String FILE_NAME6 =  (System.getProperty("user.dir"))+ "/Transit_noncog_"+ randomnumber+ ".csv"; //non cog
    public static final String FILE_NAME =  (System.getProperty("user.dir"))+ "/TcellTrack_"+ randomnumber+ ".csv";
 
    
    private Constants(){
    	;
    }
}