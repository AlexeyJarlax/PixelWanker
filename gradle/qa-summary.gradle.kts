tasks.register("qaTestSummary") {
    group = "verification"
    description = "Runs debug unit tests and prints QA summary line."
    dependsOn(":app:testDebugUnitTest")

    doLast {
        val reportDir = project(":app").file("build/test-results/testDebugUnitTest")
        val reportFiles = reportDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "xml" }
            .toList()

        var total = 0
        var failures = 0
        var errors = 0
        var skipped = 0

        reportFiles.forEach { reportFile ->
            val text = reportFile.readText()
            val tests = Regex("tests=\"(\\d+)\"").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val fileFailures = Regex("failures=\"(\\d+)\"").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val fileErrors = Regex("errors=\"(\\d+)\"").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val fileSkipped = Regex("skipped=\"(\\d+)\"").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            total += tests
            failures += fileFailures
            errors += fileErrors
            skipped += fileSkipped
        }

        val passed = (total - failures - errors - skipped).coerceAtLeast(0)
        val successRate = if (total == 0) 0 else ((passed * 100.0) / total).toInt()

        println("[QA][SUMMARY]: $successRate% of the tests were successfully completed")
        println("[QA][SUMMARY]: total=$total, passed=$passed, failed=$failures, errors=$errors, skipped=$skipped")
    }
}
