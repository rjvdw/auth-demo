import { FormSubmitError } from './errors/form-submit-error.js'

/**
 * Submit a form using `fetch`.
 *
 * @template T The expected type of the response.
 * @param {HTMLFormElement} form The form to submit.
 * @returns {Promise<T>} The response as returned by the `fetch` call.
 * @throws {FormSubmitError} If the `fetch` call fails.
 */
export async function handleForm(form) {
  const response = await fetch(form.action, {
    method: form.method,
    body: new FormData(form),
  })

  if (!response.ok) {
    throw new FormSubmitError(form, response)
  }

  return response.json()
}

/**
 * Returns the controls inside a given form.
 *
 * @param {HTMLFormElement} form
 * @returns {Array<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement | HTMLButtonElement>}
 */
export function selectControls(form) {
  return Array.from(form.querySelectorAll('input, textarea, select, button'))
}
