import { HttpError } from './errors/http-error.js'

/**
 * Validate the generated credential.
 *
 * @template T The type of the request body.
 * @template U The expected type of the response.
 * @param {string} url The URL to call.
 * @param {T} body The request body.
 * @returns {Promise<U>} The validation result.
 * @throws {HttpError} If the validation request fails.
 */
export async function post(url, body) {
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  })

  if (!response.ok) {
    throw new HttpError(response)
  }

  return response.json()
}
