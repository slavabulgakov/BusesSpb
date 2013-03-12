package ru.slavabulgakov.busesspb;

public class Mercator {
	final private static double R_MAJOR = 6378137.0;
    final private static double R_MINOR = 6356752.3142;
 
    public double[] merc(double x, double y) {
        return new double[] {mercX(x), mercY(y)};
    }
 
    private double  mercX(double lon) {
        return R_MAJOR * Math.toRadians(lon);
    }
 
    private double mercY(double lat) {
        if (lat > 89.5) {
            lat = 89.5;
        }
        if (lat < -89.5) {
            lat = -89.5;
        }
        double temp = R_MINOR / R_MAJOR;
        double es = 1.0 - (temp * temp);
        double eccent = Math.sqrt(es);
        double phi = Math.toRadians(lat);
        double sinphi = Math.sin(phi);
        double con = eccent * sinphi;
        double com = 0.5 * eccent;
        con = Math.pow(((1.0-con)/(1.0+con)), com);
        double ts = Math.tan(0.5 * ((Math.PI*0.5) - phi))/con;
        double y = 0 - R_MAJOR * Math.log(ts);
        return y;
    }
    
    
    final private static double MAX_LAT = 89.5;
    
    private double pi_2() {
    	return Math.PI / 2;
    }
    
    private double eccent() {
    	return Math.sqrt(1 - Math.pow(R_MINOR / R_MAJOR, 2));
    }
    
    private double eccnth() {
    	return eccent() * 0.5;
    }
    
    private double deg_rad() {
    	return Math.PI / 180.0;
    }
    
    private double rad_deg() {
    	return 180.0 / Math.PI;
    }

    public double merc_x(double longitude) {
    	return longitude * deg_rad() * R_MAJOR;
    }

    public double unmerc_x(double longitude) {
    	return longitude * rad_deg() / R_MAJOR;
    }

    public double merc_y(double latitude) {
    	if (latitude > MAX_LAT) latitude = MAX_LAT;
    	if (latitude < -MAX_LAT) latitude = -MAX_LAT;

    	double phi = latitude * deg_rad();
    	double con = eccent() * Math.sin(phi);
    	con = Math.pow( (1.0 - con) / (1.0 + con), eccnth());

    	return -R_MAJOR * Math.log( Math.tan(0.5 * (pi_2() - phi)) / con );
    }

    public double unmerc_y(double y) {
    	double ts = Math.exp(-y / R_MAJOR);
    	double phi = pi_2() - 2.0 * Math.atan(ts);

    	int i = 0;
    	double dPhi = 1;
    	while( (dPhi >= 0 ? dPhi : -dPhi) > 0.000000001 && i++ < 60 )
    	{
    		double con = eccent() * Math.sin(phi);
    		dPhi = pi_2() - 2.0 * Math.atan (ts * Math.pow((1.0 - con) / (1.0 + con), eccnth())) - phi;
    		phi += dPhi;
    	}

    	return phi * rad_deg();
    }
    
    public double deg2rad(double d)
	{
		double r=d*(Math.PI/180.0);
		return r;
	}
    public double rad2deg(double r)
	{
		double d=r/(Math.PI/180.0);
		return d;
	}
    
    public double pj_phi2(double ts, double e) 
	{
		int N_ITER=100;
		double HALFPI=Math.PI/2;
 
		double TOL=0.0000000001;
		double eccnth, Phi, con, dphi;
		int i;
		eccnth = .5 * e;
		Phi = HALFPI - 2. * Math.atan (ts);
		i = N_ITER;
		do 
		{
			con = e * Math.sin (Phi);
			dphi = HALFPI - 2. * Math.atan (ts * Math.pow((1. - con) / (1. + con), eccnth)) - Phi;
			Phi += dphi;
			--i;
		} 
		while ( Math.abs(dphi)>TOL && i > 0);
		return Phi;
	}
    
    enum AxisType {
    	LNG,
    	LAT
    };
    
    public double deg(double mer, AxisType axis) {
    	double box_left_mer = 	3272267.2330292;
    	double box_up_mer = 	8264094.7670049;
    	double box_right_mer = 	3479564.4537096;
    	double box_bottom_mer = 8483621.912209;
    	double box_left_deg = 	29.395276695628;
    	double box_up_deg =		59.385115204298;
    	double box_right_deg =	31.257459312814;
    	double box_bottom_deg =	60.374631272666;
    	
    	double delta_lng_mer =	box_right_mer - box_left_mer;
    	double delta_lat_mer =	box_bottom_mer - box_up_mer;
    	double delta_lng_deg =	box_right_deg - box_left_deg;
    	double delta_lat_deg =	box_bottom_deg - box_up_deg;
    	
    	double lng_coeff = 		delta_lng_deg / delta_lng_mer;
    	double lat_coeff =		delta_lat_deg / delta_lat_mer;
    	
    	double coeff;
    	double start_deg;
    	double start_mer;
    	if (axis == AxisType.LNG) {// долгота
			coeff = lng_coeff;
			start_deg = box_left_deg;
			start_mer = box_left_mer;
		} else {
			coeff = lat_coeff;
			start_deg = box_up_deg + .0035;
			start_mer = box_up_mer;
		}
    	
    	return start_deg + (mer - start_mer) * coeff;
    }
}
