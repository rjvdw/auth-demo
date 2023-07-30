import { get } from 'https://unpkg.com/@github/webauthn-json?module'
import { handleForm, selectControls } from './lib/form.js'
import { post } from './lib/http.js'

document.getElementById('login-form').addEventListener('submit', async (event) => {
  event.preventDefault()
  const controls = selectControls(event.target).filter((el) => !el.disabled)

  try {
    const user = new FormData(event.target).get('user')
    const options = await handleForm(event.target)
    controls.forEach((el) => (el.disabled = true))
    const credential = await get(options)
    const result = await post('/auth/login/validate', {
      user,
      credentialJson: JSON.stringify(credential),
    })

    console.log(result)
  } catch (error) {
    console.error(error)
  } finally {
    controls.forEach((el) => (el.disabled = false))
  }
})
