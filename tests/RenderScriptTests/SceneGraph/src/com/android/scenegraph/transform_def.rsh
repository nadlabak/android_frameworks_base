// Copyright (C) 2009 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#pragma version(1)

#pragma rs java_package_name(com.android.scenegraph)

#include "rs_graphics.rsh"

#define TRANSFORM_NONE 0
#define TRANSFORM_TRANSLATE 1
#define TRANSFORM_ROTATE 2
#define TRANSFORM_SCALE 3

static void printName(rs_allocation name) {
    rsDebug("Object Name: ", 0);
    if (!rsIsObject(name)) {
        rsDebug("no name", 0);
        return;
    }

    rsDebug((const char*)rsGetElementAt(name, 0), 0);
}

typedef struct __attribute__((packed, aligned(4))) SgTransform {
    rs_matrix4x4 globalMat;
    rs_matrix4x4 localMat;

    float4 transforms[16];
    int transformTypes[16];
    rs_allocation transformNames[16];

    int isDirty;

    rs_allocation children;

    rs_allocation name;
} SgTransform;

typedef struct RenderState_s {
    rs_program_vertex pv;
    rs_program_fragment pf;
    rs_program_store ps;
    rs_program_raster pr;
} SgRenderState;

typedef struct Renderable_s {
    rs_allocation render_state;
    rs_allocation pv_const;
    rs_allocation pf_const;
    rs_allocation pf_textures[8];
    int pf_num_textures;
    rs_mesh mesh;
    int meshIndex;
    rs_allocation transformMatrix;
    rs_allocation name;
    float4 boundingSphere;
    float4 worldBoundingSphere;
    int bVolInitialized;
    int cullType; // specifies whether to frustum cull
} SgRenderable;

typedef struct RenderPass_s {
    rs_allocation color_target;
    rs_allocation depth_target;
    rs_allocation camera;
    rs_allocation objects;

    float4 clear_color;
    float clear_depth;
    bool should_clear_color;
    bool should_clear_depth;
} SgRenderPass;

typedef struct __attribute__((packed, aligned(4))) Camera_s {
    rs_matrix4x4 proj;
    rs_matrix4x4 view;
    rs_matrix4x4 viewProj;
    float4 position;
    float near;
    float far;
    float horizontalFOV;
    float aspect;
    rs_allocation name;
    rs_allocation transformMatrix;
} SgCamera;

#define LIGHT_POINT 0
#define LIGHT_DIRECTIONAL 1

typedef struct __attribute__((packed, aligned(4))) Light_s {
    float4 position;
    float4 color;
    float intensity;
    int type;
    rs_allocation name;
    rs_allocation transformMatrix;
} SgLight;

typedef struct VShaderParams_s {
    rs_matrix4x4 model;
    rs_matrix4x4 viewProj;
} VShaderParams;

typedef struct FShaderParams_s {
    float4 cameraPos;
} FShaderParams;

typedef struct FBlurOffsets_s {
    float blurOffset0;
    float blurOffset1;
    float blurOffset2;
    float blurOffset3;
} FBlurOffsets;

typedef struct VertexShaderInputs_s {
    float4 position;
    float3 normal;
    float2 texture0;
} VertexShaderInputs;

static void printCameraInfo(SgCamera *cam) {
    rsDebug("***** Camera information. ptr:", cam);
    printName(cam->name);
    const SgTransform *camTransform = (const SgTransform *)rsGetElementAt(cam->transformMatrix, 0);
    rsDebug("Transform name:", camTransform);
    printName(camTransform->name);

    rsDebug("Aspect: ", cam->aspect);
    rsDebug("Near: ", cam->near);
    rsDebug("Far: ", cam->far);
    rsDebug("Fov: ", cam->horizontalFOV);
    rsDebug("Position: ", cam->position);
    rsDebug("Proj: ", &cam->proj);
    rsDebug("View: ", &cam->view);
}

static void printLightInfo(SgLight *light) {
    rsDebug("***** Light information. ptr:", light);
    printName(light->name);
    const SgTransform *lTransform = (const SgTransform *)rsGetElementAt(light->transformMatrix, 0);
    rsDebug("Transform name:", lTransform);
    printName(lTransform->name);

    rsDebug("Position: ", light->position);
    rsDebug("Color : ", light->color);
    rsDebug("Intensity: ", light->intensity);
    rsDebug("Type: ", light->type);
}

static void getCameraRay(const SgCamera *cam, int screenX, int screenY, float3 *pnt, float3 *vec) {
    rsDebug("=================================", screenX);
    rsDebug("Point X", screenX);
    rsDebug("Point Y", screenY);

    rs_matrix4x4 mvpInv;
    rsMatrixLoad(&mvpInv, &cam->viewProj);
    rsMatrixInverse(&mvpInv);

    float width = (float)rsgGetWidth();
    float height = (float)rsgGetHeight();

    float4 pos = {(float)screenX, height - (float)screenY, 0.0f, 1.0f};

    pos.x /= width;
    pos.y /= height;

    rsDebug("Pre Norm X", pos.x);
    rsDebug("Pre Norm Y", pos.y);

    pos.xy = pos.xy * 2.0f - 1.0f;

    rsDebug("Norm X", pos.x);
    rsDebug("Norm Y", pos.y);

    pos = rsMatrixMultiply(&mvpInv, pos);
    float oneOverW = 1.0f / pos.w;
    pos.xyz *= oneOverW;

    rsDebug("World X", pos.x);
    rsDebug("World Y", pos.y);
    rsDebug("World Z", pos.z);

    rsDebug("Cam X", cam->position.x);
    rsDebug("Cam Y", cam->position.y);
    rsDebug("Cam Z", cam->position.z);

    *vec = normalize(pos.xyz - cam->position.xyz);
    rsDebug("Vec X", vec->x);
    rsDebug("Vec Y", vec->y);
    rsDebug("Vec Z", vec->z);
    *pnt = cam->position.xyz;
}