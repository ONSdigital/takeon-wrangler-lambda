### **Wrangler Lambda**

A Lambda function to handle incoming requests containing Question Code and value having the following JSON format:

    {
      "responses": [
        {
          "questionCode": "601",
          "response": "146"
        },
        {
          "questionCode": "602",
          "response": "150"
        },
        {
          "questionCode": "700",
          "response": "148"
        },
        {
          "questionCode": "603",
          "response": "602"
        },
        {
          "questionCode": "701",
          "response": "603"
        },
        {
          "questionCode": "605",
          "response": "604"
        },
        {
          "questionCode": "606",
          "response": "605"
        },
        {
          "questionCode": "607",
          "response": "606"
        },
        {
          "questionCode": "608",
          "response": "607"
        },
        {
          "questionCode": "609",
          "response": "608"
        }
      ]
    }

    Hard coded  Validation Config which has list of QuestionCode and Derived Question Code combinations for example

                Question Code 601 != Derived Question Code 700
                Question Code 602 != Derived Question Code 701


Looping through the Validation Config and prepare Json for calling Validation Lambda i.e VET

The Prepared Json would be similar to

    {
        "primaryValue":"150",
        "comparisonValue":"603",
        "metaData":null
    }

 Call the Validation Lambda for a each item in Validation Config


### **Configuration**

The following configuration is required:


WRANGLER_NAME - the name of the wrangler lambda i.e VET being called within the eu-west-2 region - takeon-val-comparison-dev-valueComparison


#### **Deployment**

https://eu-west-2.console.aws.amazon.com/lambda/home?region=eu-west-2#/functions/wrangler-lambda?tab=graph