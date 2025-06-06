# Process Comparator

**Process Comparator** is a REST service used in conjunction with the **BulkValidator** user interface. It does not include a standalone command-line interface and currently has no dedicated web pages.

## How BulkValidator and Process Comparator Work Together

1. **BulkValidator** implements the user interface (dynamically generated web pages).
2. The user initiates an action (e.g., button click or link).
3. **BulkValidator** sends REST requests to Process Comparator resources:
   - compare ER/AR,
   - highlight differences,
   - read data from data sources.
4. The Process Comparator module performs the requested action and returns the result.
5. **BulkValidator** may perform additional actions and displays the results as a web page.

Interaction between BulkValidator and Process Comparator is based on REST API. Requests and responses are JSON messages in a well-defined, human-readable format.

## Using Process Comparator as a Standalone REST Service

**External applications** can interact with Process Comparator directly by sending JSON requests to its endpoints:

- compare ER/AR,
- highlight comparison results,
- read data from data sources.

Process Comparator processes the request and returns a response in JSON format.

## Internal Structure of BulkValidator

BulkValidator uses an internal database (PostgreSQL or Oracle) to:

- store configuration,
- store ER/AR data read from sources,
- store validation results.

Through the BulkValidator UI, the user can:

- configure test cases, validation parameters, rules, and other related objects,
- read ER/AR values,
- validate ER/AR,
- view validation results,
- generate various reports.

## Quick Compare Feature

To use Process Comparator interactively without setting up test cases and parameters:

1. Navigate to the **Utils** page in BulkValidator:  
   `http://<server>:<port>/bvtool/utils.jsp`
2. Open the **Quick compare** tab.
3. Select the appropriate **content-type**.
4. Paste the reference value into the left text area.
5. Paste the actual value into the right text area.
6. (Optional) Enter up to 6 comparison rules in the name-value table at the bottom.
7. Click the **Compare** button.

This feature is useful when you need to:

- determine the correct content type for your data,
- preview comparison results,
- adjust rules to fine-tune comparator behavior.
