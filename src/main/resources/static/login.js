import { get } from 'https://unpkg.com/@github/webauthn-json?module'
import { handleForm, selectControls } from './lib/form.js'
import { HttpError } from './lib/http-error.js'

document.getElementById('login-form').addEventListener('submit', async (event) => {
  event.preventDefault()
  const controls = selectControls(event.target).filter((el) => !el.disabled)

  try {
    const user = new FormData(event.target).get('user')
    const options = await handleForm(event.target)
    controls.forEach((el) => (el.disabled = true))
    const credential = await get(options)
    const result = await validate(user, credential)

    console.log(result)
  } catch (error) {
    console.error(error)
  } finally {
    controls.forEach((el) => (el.disabled = false))
  }
})

/**
 * Validate the generated credential.
 *
 * @template T The expected type of the response.
 * @param {string} user The user for whom the credential was created.
 * @param {Credential} credential The generated credential.
 * @returns {Promise<T>} The validation result.
 * @throws {HttpError} If the validation request fails.
 */
async function validate(user, credential) {
  const response = await fetch('/auth/login/validate', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      user,
      credentialJson: JSON.stringify(credential),
    }),
  })

  if (!response.ok) {
    throw new HttpError(response)
  }

  return response.json()
}
