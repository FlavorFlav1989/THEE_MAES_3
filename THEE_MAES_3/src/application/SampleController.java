package application;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.CalculMMS;
import model.Generatrice;
import model.Ki2Classes;
import model.ProcessusPoisson;
import model.TimerPerso;
import model.TypeDistribution;

public class SampleController {
	
	CalculMMS calculmms;
	ProcessusPoisson proc_Poiss;
	Generatrice gen;
	
	@FXML
	TextField nb_ligne;
	@FXML
	TextField capa_ligne;
	@FXML
	TextField nb_arrive;
	@FXML
	TextField nb_paquet_arrive;
	@FXML
	TextField taille_paquet_arrive;
	
	@FXML
	TextField paquets_arrives;
	@FXML
	TextField paquets_pardus;
	@FXML
	TextField pourc_perte;
	@FXML
	TextField moy_paquets_sys;
	@FXML
	TextField moy_paquets_file;
	
	@FXML
	TextField lamda;
	@FXML
	TextField mu;
	@FXML
	TextField s;
	
	@FXML
	TextField nb_client;
	
	@FXML
	TextField temps_attente;
	
	@FXML
	TextField prob_nb_client;
	
	@FXML
	TextField prob_temps_attente;
	
	@FXML
	TextField taille_buffer;
	
	@FXML
	Button calculer;
	@FXML
	TextField nbs;
	@FXML
	TextField nbf;
	@FXML
	TextField nbso;
	@FXML
	TextField tas;
	@FXML
	TextField taf;
	@FXML
	TextField nbsi;
	@FXML
	VBox hbox_lc;
	
	@FXML
	NumberAxis xLineAxis = new NumberAxis();;
	
	@FXML
	NumberAxis yLineAxis = new NumberAxis();;
	
	@FXML
	NumberAxis xLinePoissAxis = new NumberAxis();
	
	@FXML
	NumberAxis yLinePoissAxis = new NumberAxis();
	
	@FXML
	LineChartWithMarkers<Number, Number> lc_poiss;
	
	@FXML
	Button ki2;
	
	LineChart<Number, Number> lc = new LineChart<Number, Number>(xLineAxis, yLineAxis);
	
	XYChart.Series<Number, Number> series1_file_att;
	Set<Data<Number, Number>> series1_file_att_tmp;
	
	XYChart.Series<Number, Number> series2_paquet_sys;
	Set<Data<Number, Number>> series2_paquet_sys_tmp;
	
	XYChart.Series<Number, Number> series3_moy_paquet_sys;
	
	XYChart.Series<Number, Number> limite_bufer;
	
	int index_tot = 0;
	int actu_max = 0;
	Timeline timeline;
	
	TimerPerso timer_perso;
	int taille_paquet_glob;
	int nb_paquet_arrive_glob;
	double moyenne_sys;
	double moyenne_file;
	int nb_paquet_file_buffer;
	
	int nb_paquets_perdus;
	double pourc_buffer = 0.8;
	int over_time;
	
	Label[][] ki2_tab;
	GridPane grid = new GridPane();
	Stage stage;
	
	Ki2Classes ki2_classes;
	
	public void click_ki2(){
		stage.show();
		
	}
	
	public void init_ki2(){
		stage = new Stage();
		stage.setWidth(1000);
		stage.setHeight(300);
		stage.setScene(new Scene(grid));
	}
	
	@FXML
	public void initialize(){
		init_ki2();
		hbox_lc.getChildren().add(lc);
		
//		xLinePoissAxis.setAutoRanging(false);
//		xLinePoissAxis.setLowerBound(0);
//		xLinePoissAxis.setUpperBound(5);
//		
//		yLinePoissAxis.setAutoRanging(false);
//		yLinePoissAxis.setLowerBound(0);
//		yLinePoissAxis.setUpperBound(1);
//		lc_poiss = new LineChartWithMarkers<Number, Number>(xLinePoissAxis, yLinePoissAxis);
//		lc_poiss.setTitle("Arrivée des paquets dans le système");
//		xLineAxis.setLabel("Temps");
//		yLineAxis.setLabel("Etat");
//		hbox_lc.getChildren().add(lc_poiss);
		
	}
	
	private void init_chart(){
		xLineAxis.setAutoRanging(false);
		xLineAxis.setLowerBound(0);
		xLineAxis.setUpperBound(10);
		
		xLineAxis.setLabel("Temps");
		yLineAxis.setLabel("Nombre de paquets");
		lc.setCreateSymbols(false);
		
//		xLinePoissAxis.setLowerBound(0);
//		xLinePoissAxis.setUpperBound(5);
	
	}
	
	@FXML
	public void calcul_proba_nb_client(){
		int nb_client = Integer.parseInt(this.nb_client.getText());
		double res = calculmms.probasuppn(nb_client);
		this.prob_nb_client.setText(res+"");
	}
	
	@FXML
	public void calcul_prob_temps_att(){
		int temps_attente = Integer.parseInt(this.temps_attente.getText());
		double res = calculmms.probatempssuppt(temps_attente);
		this.prob_temps_attente.setText(res+"");
	}
	
	@FXML
	public void calcul(){
		ki2.setDisable(true);
		index_tot = 0;
		over_time = 0;
		nb_paquet_file_buffer = 0;
		nb_paquets_perdus = 0;
		nb_paquet_arrive_glob = (int)check_value(nb_paquet_arrive, 3000);
		taille_paquet_glob = (int)check_value(taille_paquet_arrive, 3200);
		
		int lamda = (int) check_value(this.nb_arrive, 40);
		int mu = (int)(check_value(this.capa_ligne, 64) * 1000) / this.taille_paquet_glob;
		int s = (int) check_value(this.nb_ligne, 2);
		
		this.lamda.setText(""+lamda);
		this.mu.setText(""+mu);
		this.s.setText(""+s);
		
		moyenne_sys = 0;
		
		calculmms = new CalculMMS(lamda, mu, s);
		proc_Poiss = new ProcessusPoisson(calculmms.getLambda(), TypeDistribution.EXPONENTIELLE);
		
		this.taille_buffer.setText(String.format("%.3f", (calculmms.getMu() * calculmms.getS() * taille_paquet_glob / 1000 * pourc_buffer)));
		set_valeur_afficher();
		init_chart();
		lc.getData().clear();
		
		series1_file_att = new XYChart.Series<Number, Number>();
		series1_file_att.setName("Evolution du nombre de paquets dans la file d'attente");
		series1_file_att_tmp = new HashSet<Data<Number, Number>>();
		
		series2_paquet_sys = new XYChart.Series<Number, Number>();
		series2_paquet_sys.setName("Evolution du nombre de paquets dans le système");
		series2_paquet_sys_tmp = new HashSet<Data<Number, Number>>();
		
		series3_moy_paquet_sys = new XYChart.Series<Number, Number>();
		series3_moy_paquet_sys.setName("Moyenne d'arrivée de paquets dans le système");
		
		limite_bufer = new XYChart.Series<Number, Number>();
		limite_bufer.setName("Limite du buffer");
		limite_bufer.getData().add(new XYChart.Data<Number, Number>(0, calculmms.getMu() * calculmms.getS() * pourc_buffer));
		limite_bufer.getData().add(new XYChart.Data<Number, Number>(1000, calculmms.getMu() * calculmms.getS() * pourc_buffer));
		
		lc.getData().add(series1_file_att);
		lc.getData().add(series2_paquet_sys);
		lc.getData().add(limite_bufer);
		
		timeline = new Timeline(new KeyFrame(
		        Duration.millis(1),
		        ae -> actu_tracer()));
		
		timeline.setCycleCount(Animation.INDEFINITE);
		
		timer_perso = new TimerPerso((int)(1000/calculmms.getLambda()), new java.util.concurrent.Callable<Integer>(){public Integer call(){return calcul_proc_poiss();}});
		timer_perso.start();
		timeline.play();
	}
	
	private void set_valeur_afficher(){
		nbs.setText(String.format("%.3f", calculmms.nbs()));
		nbf.setText(String.format("%.3f", calculmms.nbf()));
		nbso.setText(String.format("%.3f", calculmms.nbso()));
		tas.setText(String.format("%.3f", calculmms.tas()));
		taf.setText(String.format("%.3f", calculmms.taf()));
		nbsi.setText(String.format("%.3f", calculmms.nbsi()));
	}
	
	private double check_value(TextField value, double defaut){
		double to_return = defaut;
		try{
			to_return = Double.parseDouble(value.getText());
		}
		catch(Exception e){
			value.setText(defaut+"");
		}
		if(to_return == 0){
			to_return = defaut;
			value.setText(defaut+"");
		}
		return to_return;
	}
	
	private void unionArrays(List<Data<Number, Number>> arrays_1, List<Data<Number, Number>> arrays_2){
	   boolean is_present = false;
        for(int i = 0; i < arrays_2.size(); i++){
        	for(int j = 0; j < arrays_1.size(); j++){
	        	if(arrays_1.get(j).getXValue().equals(arrays_2.get(i).getXValue())){
	        		is_present = true;
	        		break;
	        	}	
	        }
        	if(!is_present){
        		arrays_1.add(arrays_2.get(i));
        	}
        	is_present = false;
        }
    }
	
	public void remplir_tab(){
		for(int i = 0; i < ki2_tab.length; i++){
			for(int j = 0; j < ki2_tab[i].length; j++){
				if(j == 0){
					if(i == 0)
						ki2_tab[i][j].setText("Classes ");
					else if(i == 1)
						ki2_tab[i][j].setText("Bornes ");
					else if(i == 1)
						ki2_tab[i][j].setText("Valeurs réel ");
					else if(i == 2)
						ki2_tab[i][j].setText("Valeurs théorique ");
					else if(i == 3)
						ki2_tab[i][j].setText("ki2 ");
					else if(i == 4){
						ki2_tab[i][j].setText(String.format("%.2f",ki2_classes.ki2()));
						ki2_tab[i][j].setTextFill(Color.RED);
					}
						
				}
				else{
					if(i == 0)
						ki2_tab[i][j].setText("" + (j-1));
					else if(i == 1)
						ki2_tab[i][j].setText("[" + String.format("%.2f",ki2_classes.getBorne_inf().get(j-1)) + "; " + String.format("%.2f", ki2_classes.getBorne_supp().get(j-1)) + "]");
					else if(i == 2)
						ki2_tab[i][j].setText("" + ki2_classes.getValeurs_reel().get(j-1));
					else if(i == 3)
						ki2_tab[i][j].setText("" + String.format("%.2f", ki2_classes.getValeurs_th().get(j-1)));
					else if(i == 4)
						ki2_tab[i][j].setText("" + String.format("%.2f", ki2_classes.getKi2_vals().get(j-1)));
				}
			}
		} 
	}
	
	public void creer_grid_label(){
		for(int i = 0; i < ki2_tab.length; i++){
			for(int j = 0; j < ki2_tab[i].length; j++){
				ki2_tab[i][j] = new Label();
				GridPane.setRowIndex(ki2_tab[i][j], i);
				GridPane.setColumnIndex(ki2_tab[i][j], j);
				grid.getChildren().add(ki2_tab[i][j]);
			}
		}
	}
	
	private void actu_tracer(){	
		//series1_expo = new XYChart.Series<Number, Number>();
		List<Data<Number, Number>> list = new ArrayList<Data<Number, Number>>(series1_file_att_tmp);
		unionArrays(series1_file_att.getData(), list);
		
		List<Data<Number, Number>> list2 = new ArrayList<Data<Number, Number>>(series2_paquet_sys_tmp);
		unionArrays(series2_paquet_sys.getData(), list2);
		
			if(index_tot >= nb_paquet_arrive_glob){
				
				timer_perso.arreter_timer();
				if(nb_paquet_file_buffer > 0){
					over_time += 1;
					int nb_paquet_pass = nb_paquet_file_buffer;
					if(nb_paquet_pass >= (calculmms.getMu() * calculmms.getS() * pourc_buffer))
						nb_paquet_pass -= (calculmms.getMu() * calculmms.getS() * pourc_buffer);
					else
						nb_paquet_pass = 0;
					series1_file_att_tmp.add(new XYChart.Data((proc_Poiss.max_time()-1) + over_time, nb_paquet_pass));
					nb_paquet_file_buffer = nb_paquet_pass;
					avance_chart();
				}
				else{
					timeline.stop();
					ki2_classes = proc_Poiss.ki2();
					ki2_tab = new Label[5][ki2_classes.getTaille()+1];
					creer_grid_label();
					remplir_tab();
					ki2.setDisable(false);
				}
					
				/*series3_moy_paquet_sys.getData().add(new XYChart.Data(0, moyenne_sys));
				series3_moy_paquet_sys.getData().add(new XYChart.Data(index_tot, moyenne_sys));*/
			}
			else{
				index_tot = proc_Poiss.getListElemEcart().size();	
				paquets_arrives.setText("" + index_tot);	
				moy_paquets_sys.setText(String.format("%.3f", moyenne_sys));
				moy_paquets_file.setText(String.format("%.3f", moyenne_file));
				avance_chart();	
				//tracer_lines();
			}
	}
	public Integer calcul_proc_poiss(){
		proc_Poiss.ajout_elem();
		try{
			if(actu_max != (proc_Poiss.max_time()-1)){
				int max_capa = (int)(calculmms.getMu() * calculmms.getS() * taille_paquet_glob);
				int nb_paquet = proc_Poiss.compte_in_max(proc_Poiss.max_time()-1) + nb_paquet_file_buffer;
				int taille_paquets = nb_paquet * taille_paquet_glob;
				int nb_paquet_file = 0;
				
				if(max_capa < taille_paquets)
					nb_paquet_file = (taille_paquets- max_capa) / taille_paquet_glob;
				
				if(nb_paquet_file >= (calculmms.getMu() * calculmms.getS() * pourc_buffer)){
					nb_paquets_perdus += (nb_paquet_file - (int)(calculmms.getMu() * calculmms.getS() * pourc_buffer));
					nb_paquet_file = (int)(calculmms.getMu() * calculmms.getS()* pourc_buffer);
				}
					
				System.out.println("paquets arrivé total " + index_tot);
				System.out.println("paquets perdus total " + nb_paquets_perdus);
				
				nb_paquet_file_buffer = nb_paquet_file;
				series1_file_att_tmp.add(new XYChart.Data(proc_Poiss.max_time()-1, nb_paquet_file));
				series2_paquet_sys_tmp.add(new XYChart.Data(proc_Poiss.max_time()-1, nb_paquet));
				
				if(proc_Poiss.max_time()-1 == 0){
					moyenne_sys = (nb_paquet);
					moyenne_file = (nb_paquet_file);
				}
				else{
					moyenne_sys = (moyenne_sys * (proc_Poiss.max_time()-2) + nb_paquet ) / (proc_Poiss.max_time()-1);
					moyenne_file = (moyenne_file * (proc_Poiss.max_time()-2) + nb_paquet_file ) / (proc_Poiss.max_time()-1);
				}
				this.paquets_pardus.setText("" + nb_paquets_perdus);
				this.pourc_perte.setText(String.format("%.3f", ((double)nb_paquets_perdus / (double)index_tot * 100.0)));
				actu_max = proc_Poiss.max_time()-1;
			}
		}
		catch(ConcurrentModificationException e){
			return 0;
		}
		return 1;
	}
	
	public void tracer_lines(){
		lc_poiss.removeAllVerticalMarker();
		List<Double> list = proc_Poiss.getListElemPointe();
		for(int i = 0; i < list.size(); i++){
			tracer_line(list.get(i), 0);
		}
	}
	public void tracer_line(double x, double y){
		 Data<Number, Number> verticalMarker = new Data<>(x, y);
	        lc_poiss.addVerticalValueMarker(verticalMarker);
	        
	        Slider verticalMarkerSlider = new Slider(xLineAxis.getLowerBound(), xLineAxis.getUpperBound(), 0);
	        verticalMarkerSlider.setOrientation(Orientation.HORIZONTAL);
	        verticalMarkerSlider.setShowTickLabels(true);
	        verticalMarkerSlider.valueProperty().bindBidirectional(verticalMarker.XValueProperty());
	        verticalMarkerSlider.minProperty().bind(xLineAxis.lowerBoundProperty());
	        verticalMarkerSlider.maxProperty().bind(xLineAxis.upperBoundProperty());
	}
	
	@SuppressWarnings("rawtypes")
	private class LineChartWithMarkers<X,Y> extends LineChart {

        private ObservableList<Data<X, Y>> horizontalMarkers;
        private ObservableList<Data<X, Y>> verticalMarkers;

        @SuppressWarnings("unchecked")
		public LineChartWithMarkers(Axis<X> xAxis, Axis<Y> yAxis) {
            super(xAxis, yAxis);
            horizontalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()});
            horizontalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
            verticalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
            verticalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
        }

        public void addHorizontalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (horizontalMarkers.contains(marker)) return;
            Line line = new Line();
            //setColor(line);
            marker.setNode(line );
            getPlotChildren().add(line);
            horizontalMarkers.add(marker);
        }

        public void removeHorizontalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (marker.getNode() != null) {
                getPlotChildren().remove(marker.getNode());
                marker.setNode(null);
            }
            horizontalMarkers.remove(marker);
        }

        public void addVerticalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (verticalMarkers.contains(marker)) return;
            Line line = new Line();
            //setColor(line);
            marker.setNode(line );
            getPlotChildren().add(line);
            verticalMarkers.add(marker);
        }

        public void removeAllVerticalMarker(){
        	for (int i = verticalMarkers.size()-1; i >=0; i--) {
        		removeVerticalValueMarker(verticalMarkers.get(i));
            }    
        }
        public void removeVerticalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (marker.getNode() != null) {
                getPlotChildren().remove(marker.getNode());
                marker.setNode(null);
            }
            verticalMarkers.remove(marker);
        }


        @Override
        protected void layoutPlotChildren() {
            super.layoutPlotChildren();
            for (Data<X, Y> horizontalMarker : horizontalMarkers) {
                Line line = (Line) horizontalMarker.getNode();
                //setColor(line);
                line.setEndX(getBoundsInLocal().getWidth());
                line.setStartY(getYAxis().getDisplayPosition(horizontalMarker.getYValue()) + 0.5); // 0.5 for crispness
                line.setEndY(line.getStartY());
                line.toFront();
            }
            for (Data<X, Y> verticalMarker : verticalMarkers) {
                Line line = (Line) verticalMarker.getNode();
                //setColor(line);
                line.setStartX(getXAxis().getDisplayPosition(verticalMarker.getXValue()) + 0.5);  // 0.5 for crispness
                line.setEndX(line.getStartX());
                line.setStartY(0d);
                line.setEndY(getBoundsInLocal().getHeight());
                line.toFront();
            }      
        }
    }
	
	public void avance_chart(){	 
//		if(proc_Poiss.max_time() > (int)xLinePoissAxis.getUpperBound()){
//			double newLower =  xLinePoissAxis.getLowerBound() + (proc_Poiss.max_time() - (int)xLinePoissAxis.getUpperBound());
//			double newUpper =  xLinePoissAxis.getUpperBound() + (proc_Poiss.max_time() - (int)xLinePoissAxis.getUpperBound());
//			xLinePoissAxis.setLowerBound(newLower);
//			xLinePoissAxis.setUpperBound(newUpper);
//		}
		
		xLineAxis.setUpperBound(proc_Poiss.max_time() + over_time);
	}
}
