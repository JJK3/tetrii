function assert_not_null(obj, message) {
	message = message ? message : ""
	message = "Assertion Error: object was null " + message
	if (obj == null || !obj){
		throw message;
	}
}

function assert(bool, message) {
	message = message ? message : ""
	message = "Assertion Error: " + message
	if (!bool){
		throw message;
	}
}

function array_equal(arr1, arr2) {
	if (arr1.length != arr2.length) return false;
	for (var i=0; i<arr1.length; i++) {
		if (arr1[i] instanceof(Array)) {
			if (!array_equal(arr1[i], arr2[i])) {
				return false;
			}
		} else {
			if (arr1[i] != arr2[i]){
				return false;
			}
		}
	}
	return true;
}

function assert_equal(expected, actual, message) {
	message = message ? message : ""
	message = "Assertion Error.  Expected " + expected + " but was " + actual + ". " + message
	if (expected instanceof(Array)) {
		if (!array_equal(expected, actual)){
			throw message;
		}
	} else if (expected != actual){
		throw message;
	}
}

function assert_piece(expected_coords, actual_piece, message) {
	message = message ? message : ""
	var real_coords = [];
	for (var i=0; i<actual_piece.blocks.length; i++) {
		var b = actual_piece.blocks[i];
		real_coords.push([b.x, b.y]);
	}
	message = "Assertion Error.  Expected " + expected_coords + " but was " + real_coords + ". " + message
	if (!array_equal(expected_coords, real_coords)){
		throw message;
	}
}


function test(testFunction) {
	try {
		testFunction();
	} catch (err){
		return err;
	}
	return null;
}

function run_test_suite(suite) {
	var errors = {}
	var errorCount = 0
	var result = ""
	var total_tests = 0;
	for (var test_name in suite.tests) {
		total_tests++;
		var test_result = test(suite.tests[test_name])
		if (test_result != null){
			errors[test_name] = test_result;
			errorCount++;
		}
	}
	for (var erredTest in errors) {
		result += erredTest + " -> " + errors[erredTest] + "<br/>";
	}
	result += "<br/>";
	var passing_tests = total_tests - errorCount;
	var percent_passing = 100.0 * passing_tests / total_tests;
	result += suite.suite_name + ": " + passing_tests + "/" + total_tests + " (" + percent_passing + "%) tests passed"
	return result;
}


pieceTests = {
	suite_name : "Piece Tests",
	tests :  {
		"creation test" : function(){
			assert_not_null(line(1, 2), "line null test");
			assert_not_null(square(1, 2), "square null test");
			assert_not_null(l_shape1(1, 2), "lshape1 null test");
			assert_not_null(l_shape2(1, 2), "lshape2 null test");
			assert_not_null(n_shape1(1, 2), "nshape1 null test");
			assert_not_null(n_shape1(1, 2), "nshape2 null test");
			assert_not_null(line(1, 2).blocks[1], "line.blocks null test");
		},
		"piece move test" : function(){
			assert_equal(3, line(1, 2).down().center.y, "down piece test");
			assert_equal(4, line(1, 2).down().blocks[2].y, "down piece test");
			assert_equal(0, line(1, 2).left().center.x, "left piece test");
			assert_equal(0, line(1, 2).left().blocks[2].x, "left piece test");
			assert_equal(2, line(1, 2).right().center.x, "left piece test");
		},
		"piece rotation test" : function(){
			assert_piece([[2,3], [1,3], [0,3], [-1,3]], line(1, 3).rotate_clockwise(), "rotate 1");
			assert_piece([[1,4], [1,3], [1,2], [1,1]], 
						 line(1, 3).rotate_clockwise().rotate_clockwise(), "rotate 1");
			assert_piece([[0,3], [1,3], [2,3], [3,3]], 
						 line(1, 3).rotate_clockwise().rotate_clockwise().rotate_clockwise(), "rotate 1");
			assert_piece([[1,2], [1,3], [1,4], [1,5]], 
						 line(1, 3).rotate_clockwise().rotate_clockwise().rotate_clockwise().rotate_clockwise(), "rotate 1");
		}
	}
}

boardTests = {
	suite_name : "Board Tests",
	tests :  {
		"simple test1" : function(){
			var b = new Board(10, 20);
			assert_equal(null, b.get_block_at(2,2));
			b.place_piece(line(3, 5));
			assert_not_null(b.get_block_at(3, 5));
			var row1 = b.get_row(4)
			assert_not_null(row1[3]);
			assert_equal(null, row1[4]);
		},
		"complete test1" : function(){
			var b = new Board(8, 20);
			b.place_piece(line(2, 6).rotate_clockwise());
			b.place_piece(line(6, 6).rotate_clockwise());
			b.place_piece(line(2, 7).rotate_clockwise());
			b.place_piece(line(6, 7).rotate_clockwise());
			assert_equal([6, 7], b.find_completed_rows());
		},
		"remove test1" : function(){
			var b = new Board(8, 20);
			b.place_piece(line(2, 6).rotate_clockwise());
			b.place_piece(line(6, 6).rotate_clockwise());
			b.place_piece(line(2, 5).rotate_clockwise());
			b.remove_row(6);
			assert_equal([], b.find_completed_rows());
			assert_not_null(b.get_block_at(0, 6));
			assert_not_null(b.get_block_at(1, 6));
			assert_not_null(b.get_block_at(2, 6));
			assert_not_null(b.get_block_at(3, 6));
		}

	}
}
