// angular docs also did it this way. That's why disable eslint.
// eslint-disable-next-line prefer-arrow/prefer-arrow-functions
export function constructErrorMessageWithList(error: any): string {
  let errorMessage = error.error.message + '<br><br>';

  if (error.error.errors?.length > 0) {
    errorMessage += '<ul>';
    for (const message of error.error.errors) {
      errorMessage += '<li>' + message + '</li>';
    }
    errorMessage += '</ul>';
  }
  return errorMessage;
}
