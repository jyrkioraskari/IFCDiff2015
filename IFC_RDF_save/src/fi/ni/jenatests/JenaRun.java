package fi.ni.jenatests;

public class JenaRun {

    public static void main(String[] args) {
	     JenaGroundablesByManyLinks.set_test_properties();
	     JenaGroundablesByManyLinks.basic_testset();
	     
	     JenaGroundablesByLinks.set_test_properties();
	     JenaGroundablesByLinks.basic_testset();

	     JenaGroundablesByFiveLinks.set_test_properties();
	     JenaGroundablesByFiveLinks.basic_testset();
 
	     JenaStatisticsBasic.set_test_properties();
	     JenaStatisticsBasic.basic_testset();

	     JenaStatisticsBasicGrounding.set_test_properties();
	     JenaStatisticsBasicGrounding.basic_testset();

	     JenaGroundablesByLinks_SecondOrderLiterals.set_test_properties();
	     JenaGroundablesByLinks_SecondOrderLiterals.basic_testset();

    }

}
