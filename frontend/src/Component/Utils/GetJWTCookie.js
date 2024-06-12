import { jwtDecode } from "jwt-decode";

export const GetJWTCookie = async () => {
    try {
        const response = await fetch(`/api/user/cookieToJwt`, {
            method: "POST",
        });
        if (response.ok) {
            const access = response.headers.get('access');
            const refresh = response.headers.get('refresh');
            localStorage.setItem("access", access);
            localStorage.setItem("refresh", refresh);

            const jwt = access.replace("Bearer ", "");
            const decodedJWT = jwtDecode(jwt);
            localStorage.setItem("userId", decodedJWT.userId);
            localStorage.setItem("email", decodedJWT.iat); // 생성시간을 이메일로 설정(고유값)
            localStorage.setItem("nickname", decodedJWT.username);

            const data = await response.text();
            console.log(data);
        }
    } catch (error) {
        console.error('Error:', error);
    } finally {
        window.location.href = "/organization";
    }
};

