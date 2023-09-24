package fi.ni.vo;

public class GroundSettings {
    boolean set_literals=false;
    boolean test_unique_loops=false;
    boolean ifcOwnerHistory_handle_as_grounded=false;
    boolean ifcGeometricRepresentationContext_ground=false;
    boolean ground_On_One2OneLinks_on=false;
    boolean ground_AnyLinks_on=false;
    boolean use_distance_from_core=false;
    boolean second_order_literals=false;
    boolean global_unique_naming=false;

    public GroundSettings(boolean ifcOwnerHistory_handle_as_grounded,boolean set_literals, boolean test_unique_loops, boolean ifcGeometricRepresentationContext_ground, boolean ground_On_One2OneLinks_on, boolean ground_AnyLinks_on,
		    boolean use_distance_from_core,boolean second_order_literals,boolean global_unique_naming) {
		super();
		this.set_literals = set_literals;
		this.test_unique_loops = test_unique_loops;
		this.ifcOwnerHistory_handle_as_grounded = ifcOwnerHistory_handle_as_grounded;
		this.ifcGeometricRepresentationContext_ground = ifcGeometricRepresentationContext_ground;
		this.ground_On_One2OneLinks_on = ground_On_One2OneLinks_on;
		this.ground_AnyLinks_on = ground_AnyLinks_on;
		this.use_distance_from_core = use_distance_from_core;
		this.second_order_literals=second_order_literals;
		this.global_unique_naming=global_unique_naming;
	    }

    public GroundSettings(boolean ifcOwnerHistory_handle_as_grounded,boolean set_literals, boolean test_unique_loops, boolean ifcGeometricRepresentationContext_ground, boolean ground_On_One2OneLinks_on, boolean ground_AnyLinks_on,
	    boolean use_distance_from_core,boolean second_order_literals) {
	super();
	this.set_literals = set_literals;
	this.test_unique_loops = test_unique_loops;
	this.ifcOwnerHistory_handle_as_grounded = ifcOwnerHistory_handle_as_grounded;
	this.ifcGeometricRepresentationContext_ground = ifcGeometricRepresentationContext_ground;
	this.ground_On_One2OneLinks_on = ground_On_One2OneLinks_on;
	this.ground_AnyLinks_on = ground_AnyLinks_on;
	this.use_distance_from_core = use_distance_from_core;
	this.second_order_literals=second_order_literals;
    }

    public GroundSettings(boolean ifcOwnerHistory_handle_as_grounded,boolean set_literals, boolean test_unique_loops, boolean ifcGeometricRepresentationContext_ground, boolean ground_On_One2OneLinks_on, boolean ground_AnyLinks_on,
	    boolean use_distance_from_core) {
	super();
	this.set_literals = set_literals;
	this.test_unique_loops = test_unique_loops;
	this.ifcOwnerHistory_handle_as_grounded = ifcOwnerHistory_handle_as_grounded;
	this.ifcGeometricRepresentationContext_ground = ifcGeometricRepresentationContext_ground;
	this.ground_On_One2OneLinks_on = ground_On_One2OneLinks_on;
	this.ground_AnyLinks_on = ground_AnyLinks_on;
	this.use_distance_from_core = use_distance_from_core;
	this.second_order_literals=false;
    }

    public boolean isSet_literals() {
        return set_literals;
    }

    public void setSet_literals(boolean set_literals) {
        this.set_literals = set_literals;
    }

    public boolean isTest_unique_loops() {
        return test_unique_loops;
    }

    public void setTest_unique_loops(boolean test_unique_loops) {
        this.test_unique_loops = test_unique_loops;
    }


    public boolean isIfcOwnerHistory_handle_as_grounded() {
        return ifcOwnerHistory_handle_as_grounded;
    }

    public void setIfcOwnerHistory_handle_as_grounded(boolean ifcOwnerHistory_handle_as_grounded) {
        this.ifcOwnerHistory_handle_as_grounded = ifcOwnerHistory_handle_as_grounded;
    }

    public boolean isIfcGeometricRepresentationContext_ground() {
        return ifcGeometricRepresentationContext_ground;
    }

    public void setIfcGeometricRepresentationContext_ground(boolean ifcGeometricRepresentationContext_ground) {
        this.ifcGeometricRepresentationContext_ground = ifcGeometricRepresentationContext_ground;
    }

    public boolean isGround_On_One2OneLinks_on() {
        return ground_On_One2OneLinks_on;
    }

    public void setGround_On_One2OneLinks_on(boolean ground_On_One2OneLinks_on) {
        this.ground_On_One2OneLinks_on = ground_On_One2OneLinks_on;
    }

    public boolean isGround_AnyLinks_on() {
        return ground_AnyLinks_on;
    }

    public void setGround_AnyLinks_on(boolean ground_AnyLinks_on) {
        this.ground_AnyLinks_on = ground_AnyLinks_on;
    }

    public boolean isUse_distance_from_core() {
        return use_distance_from_core;
    }

    public void setUse_distance_from_core(boolean use_distance_from_core) {
        this.use_distance_from_core = use_distance_from_core;
    }

    public boolean isSecond_order_literals() {
        return second_order_literals;
    }

    public void setSecond_order_literals(boolean second_order_literals) {
        this.second_order_literals = second_order_literals;
    }

    public boolean isGlobal_unique_naming() {
        return global_unique_naming;
    }

    public void setGlobal_unique_naming(boolean global_unique_naming) {
        this.global_unique_naming = global_unique_naming;
    }


    
    
}
