function assertNotNull(obj, message) {
	message = message ? message : ""
	message = "Assertion Error: object was null " + message
	if (obj == null || !obj) {
		throw message;
	}
}

function assert(bool, message) {
	message = message ? message : ""
	message = "Assertion Error: " + message
	if (!bool) {
		throw message;
	}
}

function arrayEqual(arr1, arr2) {
	if (arr1.length != arr2.length)
		return false;
	for ( var i = 0; i < arr1.length; i++) {
		if (arr1[i] instanceof (Array)) {
			if (!arrayEqual(arr1[i], arr2[i])) {
				return false;
			}
		} else {
			if (arr1[i] != arr2[i]) {
				return false;
			}
		}
	}
	return true;
}

function assertEqual(expected, actual, message) {
	message = message ? message : ""
	message = "Assertion Error.  Expected " + expected + " but was " + actual
			+ ". " + message
	if (expected instanceof (Array)) {
		if (!arrayEqual(expected, actual)) {
			throw message;
		}
	} else if (expected != actual) {
		throw message;
	}
}

function assertPiece(expected_coords, actual_piece, message) {
	message = message ? message : ""
	var real_coords = [];
	for ( var i = 0; i < actual_piece.blocks.length; i++) {
		var b = actual_piece.blocks[i];
		real_coords.push([b.x, b.y]);
	}
	message = "Assertion Error.  Expected " + expected_coords + " but was "
			+ real_coords + ". " + message
	if (!arrayEqual(expected_coords, real_coords)) {
		throw message;
	}
}

function test(testFunction) {
	try {
		testFunction();
	} catch (err) {
		return err;
	}
	return null;
}

function runTestSuite(suite) {
	var errors = {}
	var errorCount = 0
	var result = ""
	var total_tests = 0;
	for ( var test_name in suite.tests) {
		total_tests++;
		var test_result = test(suite.tests[test_name])
		if (test_result != null) {
			errors[test_name] = test_result;
			errorCount++;
		}
	}
	for ( var erredTest in errors) {
		result += erredTest + " -> " + errors[erredTest] + "<br/>";
	}
	result += "<br/>";
	var passing_tests = total_tests - errorCount;
	var percent_passing = 100.0 * passing_tests / total_tests;
	result += suite.suite_name + ": " + passing_tests + "/" + total_tests
			+ " (" + percent_passing + "%) tests passed"
	return result;
}

pieceTests = {
	suite_name : "Piece Tests",
	tests : {
		"creation test" : function() {
			assertNotNull(line.move(1, 2), "line null test");
			assertNotNull(square.move(1, 2), "square null test");
			assertNotNull(l_shape1.move(1, 2), "lshape1 null test");
			assertNotNull(l_shape2.move(1, 2), "lshape2 null test");
			assertNotNull(n_shape1.move(1, 2), "nshape1 null test");
			assertNotNull(n_shape1.move(1, 2), "nshape2 null test");
			assertNotNull(line.move(1, 2).blocks[1], "line.blocks null test");
		},
		"piece move test" : function() {
			assertEqual(3, line.move(1, 2).move(0, 1).blocks[1].y,
					"down piece test");
			assertEqual(4, line.move(1, 2).move(0, 1).blocks[2].y,
					"down piece test");
			assertEqual(0, line.move(1, 2).move(-1, 0).blocks[1].x,
					"left piece test 1");
			assertEqual(0, line.move(1, 2).move(-1, 0).blocks[2].x,
					"left piece test 2");
			assertEqual(2, line.move(1, 2).move(1, 0).blocks[1].x,
					"left piece test 3");
		},
		"piece rotation test" : function() {
			assertPiece([[2, 3], [1, 3], [0, 3], [-1, 3]], line.move(1, 3)
					.rotateClockwise(), "rotate 1");
			assertPiece([[1, 4], [1, 3], [1, 2], [1, 1]], line.move(1, 3)
					.rotateClockwise().rotateClockwise(), "rotate 1");
			assertPiece([[0, 3], [1, 3], [2, 3], [3, 3]], line.move(1, 3)
					.rotateClockwise().rotateClockwise().rotateClockwise(),
					"rotate 1");
			assertPiece([[1, 2], [1, 3], [1, 4], [1, 5]], line.move(1, 3)
					.rotateClockwise().rotateClockwise().rotateClockwise()
					.rotateClockwise(), "rotate 1");
		}
	}
}

boardTests = {
	suite_name : "Board Tests",
	tests : {
		"simple test1" : function() {
			var b = new Board(10, 20);
			assertEqual(null, b.getBlockAt(2, 2));
			b.placePiece(line.move(3, 5));
			assertNotNull(b.getBlockAt(3, 5));
		},
		"complete test1" : function() {
			var b = new Board(8, 20);
			b.placePiece(line.move(2, 6).rotateClockwise());
			b.placePiece(line.move(6, 6).rotateClockwise());
			b.placePiece(line.move(2, 7).rotateClockwise());
			b.placePiece(line.move(6, 7).rotateClockwise());
			assertEqual([6, 7], b.find_completed_rows());
		},
		"remove test1" : function() {
			var b = new Board(8, 20);
			b.placePiece(line.move(2, 6).rotateClockwise());
			b.placePiece(line.move(6, 6).rotateClockwise());
			b.placePiece(line.move(2, 5).rotateClockwise());
			b.removeRow(6);
			assertEqual([], b.find_completed_rows());
			assertNotNull(b.getBlockAt(0, 6));
			assertNotNull(b.getBlockAt(1, 6));
			assertNotNull(b.getBlockAt(2, 6));
			assertNotNull(b.getBlockAt(3, 6));
		}

	}
}
