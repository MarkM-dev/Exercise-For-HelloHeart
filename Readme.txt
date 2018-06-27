The purpose of this challenge is to evaluate your technical skills and learning capabilities. This is a home assignment and you are encouraged to use any resource at your disposal. The assignment should run on Android (could be written Native or React Native)

**Please acknowledge receiving this email before proceeding**

Setup 

    Prepare a simple mobile application named HeartMe

    The application should have a single page called "Submit blood test results" and allow user interaction

Functionality

    The app should allow the user to submit data which simulates blood test result in the following structure:

        Test name - String in free text (e.g. "Total HDL Cholesterol")

        Test result value - Number (e.g. 40)

    Upon submission of the data, the app will:

        Analyze the provided data using external dataset (see "Dataset" below)

        Identify the category of the test by parsing the Test name's free text

        Evaluate the result value based on the test category and the result threshold

        Inform the user whether their result is "Good!" (i.e. below threshold), "Bad!" (i.e. above threshold) or “Unknown” (i.e test not found in dataset)

    UI mock:


Dataset

The dataset can be found in a public bucket in s3. 

    S3 bucket name: s3.helloheart.home.assignment.

    Filename: bloodTestConfig.json

    Region: US-EAST1

    Can be found here: https://s3.amazonaws.com/s3.helloheart.home.assignment/bloodTestConfig.json 


* Note: This dataset is an initial example. Don't worry about clinical accuracy ;) 

Important implementation notes

    Expect user input to be within the following character set: 'A-Z', 'a-z', '0-9' and '(),-:/!'

    Forgive user errors - Analyze the input according to dataset's list of test names but give a little leeway for different word ordering, punctuation and typos


Examples

    User input - "Cholesterol - HDL" with the value 39. Output: "HDL Cholesterol" and "Good!"

    User input - "HDL, Total" with the value 50. Output: "HDL Cholesterol" and "Bad!"

    User input - "CHOLESTEROL-LDL calc" with the value 99. Output: "HDL Cholesterol" and "Good!"

    User input - "HM Hemoglobin - A1C" with the value 12. Output: "A1C" and "Bad!"

    User input - "Triglycerides" with the value 120. Output: "Unknown"