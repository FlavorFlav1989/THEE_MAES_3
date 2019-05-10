package model;

public class CalculMMS {
	/*Cadence d'arrivée*/
	private double lambda;
	/*Cadence de traitement*/
	private double mu;
	/*Nombre de station*/
	private double s;
	/*Facteur de charge*/
	private double phi;
	
	public CalculMMS(double lambda, double mu, double s){
		this.lambda = lambda;
		this.mu = mu;
		this.s = s;
		this.phi = lambda / mu;
		
		System.out.println(lambda);
		System.out.println(mu);
		System.out.println(s);
		System.out.println(phi);
	}
	
	/**
	 * Probabilité que le nombre de paquets en attente soit supérieur à n
	 * @param n
	 * @return
	 */
	public double probasuppn(int n){
		double res = petoile0();
		for(int i = 1; i < n; i++){
			if(n < s)
				res += (petoileninf(i));
			else
				res += (petoilensup(i));
		}
		return res;
	}
	
	/**
	 * Probabilité que le temps d'attente dans la file soit supérieur à t
	 * @return
	 */
	public double probatempssuppt(int t){
		return 1 - (petoile0() * Math.pow(phi, s) * ((s * (s * mu - lambda)) / factorielle(s)) * (Math.exp(-(s * mu - lambda) * t)));
	}
	/**
	 * Probabilite qu'il y ai n client dans le système avec n < s
	 * @param n
	 * @return
	 */
	public double petoileninf(int n){
		return (Math.pow(phi, n) / factorielle(n)) * (petoile0());
	}
	
	/**
	 * Probabilite qu'il y ai n client dans le système avec n > s
	 * @param n
	 * @return
	 */
	public double petoilensup(int n){
		return (Math.pow(phi, n) / (factorielle(s) * Math.pow(s, n - s))) * (petoile0());
	}
		
	/**
	 * Probabilité qu'il y ai 0 client dans le système
	 * @return
	 */
	public double petoile0(){
		return (1 / (sommepetoile0() + (Math.pow(phi, s) / (factorielle(s) * (1 - phi/s)))));
	}
	
	/**
	 * Nombre moyen de client dans le système
	 * @return
	 */
	public double nbs(){
		return phi + ((s / ((s - phi) * (s - phi))) * (Math.pow(phi, s+1) / factorielle(s)) * (petoile0()));
	}
	
	/**
	 * Nombre moyen de client dans la file
	 * @return
	 */
	public double nbf(){
			return (s / ((s - phi) * (s - phi))) * ((Math.pow(phi, s+1)) / (factorielle(s))) * (petoile0());
	}
	
	/**
	 * Facteur de charge
	 * @return
	 */
	public double nbso(){
		return phi;
	}
	
	/**
	 * Temps d'attente moyen dans le système
	 * @return
	 */
	public double tas(){
		return nbs() / lambda;
	}
	
	/**
	 * Temps d'attente moyen dans la file d'attente
	 * @return
	 */
	public double taf(){
		return nbf() / lambda;
	}
	
	/**
	 * Nombre de station innocupées / fraction d'innactivité
	 * @return
	 */
	public double nbsi(){
		return s - phi;
	}
	
	/**
	 * Caclul de la somme dans la formule du p*0
	 * @return
	 */
	private double sommepetoile0(){
		double res = 0;
		for(int i = 0; i < s; i++){
			res += (Math.pow(phi, i) / factorielle(i));
		}
		return res;
	}
	
	/**
	 * Calcul d'une factorielle
	 * @param n
	 * @return
	 */
	private int factorielle(double n){
		int res = 1;
		for(double i = n; i > 0; i--){
			res*=i;
		}
		return res;
	}

	/**
	 * @return the lambda
	 */
	public double getLambda() {
		return lambda;
	}

	/**
	 * @param lambda the lambda to set
	 */
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	/**
	 * @return the mu
	 */
	public double getMu() {
		return mu;
	}

	/**
	 * @param mu the mu to set
	 */
	public void setMu(double mu) {
		this.mu = mu;
	}

	/**
	 * @return the s
	 */
	public double getS() {
		return s;
	}

	/**
	 * @param s the s to set
	 */
	public void setS(double s) {
		this.s = s;
	}

	/**
	 * @return the phi
	 */
	public double getPhi() {
		return phi;
	}

	/**
	 * @param phi the phi to set
	 */
	public void setPhi(double phi) {
		this.phi = phi;
	}
}
